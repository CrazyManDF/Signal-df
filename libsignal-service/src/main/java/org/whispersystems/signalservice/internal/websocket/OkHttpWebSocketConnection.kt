package org.whispersystems.signalservice.internal.websocket

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.SingleSubject
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.withLock
import org.signal.libsignal.protocol.logging.Log
import org.signal.libsignal.protocol.util.Pair
import org.whispersystems.signalservice.api.push.TrustStore
import org.whispersystems.signalservice.api.util.CredentialsProvider
import org.whispersystems.signalservice.api.util.Tls12SocketFactory
import org.whispersystems.signalservice.api.util.TlsProxySocketFactory
import org.whispersystems.signalservice.api.websocket.HealthMonitor
import org.whispersystems.signalservice.api.websocket.WebSocketConnectionState
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration
import org.whispersystems.signalservice.internal.configuration.SignalServiceUrl
import org.whispersystems.signalservice.internal.util.BlacklistingTrustManager
import org.whispersystems.signalservice.internal.util.Util
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.LinkedList
import java.util.Optional
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.max

class OkHttpWebSocketConnection(
    override var name: String,
    private val serviceConfiguration: SignalServiceConfiguration,
    private val credentialsProvider: Optional<CredentialsProvider>,
    private val signalAgent: String?,
    private val healthMonitor: HealthMonitor,
    private val extraPathUri: String,
    private val allowStories: Boolean
) : WebSocketListener(), WebSocketConnection {

    constructor(
        name: String,
        serviceConfiguration: SignalServiceConfiguration,
        credentialsProvider: Optional<CredentialsProvider>,
        signalAgent: String?,
        healthMonitor: HealthMonitor,
        allowStories: Boolean
    ) : this(
        name,
        serviceConfiguration,
        credentialsProvider,
        signalAgent,
        healthMonitor,
        "",
        allowStories
    )

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private val trustStore = serviceConfiguration.signalServiceUrls[0].trustStore
    private val interceptors = serviceConfiguration.networkInterceptors
    private val dns = serviceConfiguration.dns
    private val signalProxy = serviceConfiguration.signalProxy
    private val webSocketState =
        BehaviorSubject.createDefault(WebSocketConnectionState.DISCONNECTED)
    private val serviceUrls = serviceConfiguration.signalServiceUrls
    private val random = SecureRandom()
    private var client: WebSocket? = null

    private val incomingRequests = LinkedList<WebSocketRequestMessage>()
    private val outgoingRequests = hashMapOf<Long, OutgoingRequest>()
    private val keepAlives = hashSetOf<Long>()

    init {
        name = "[" + name + ":" + System.identityHashCode(this) + "]"
    }

    override fun connect(): Observable<WebSocketConnectionState> = lock.withLock {
        log("connect()")
        if (client == null) {
            val connectionInfo = getConnectionInfo()
            val serviceUrl = connectionInfo.first()
            val wsUri = connectionInfo.second()
            val filledUri = if (credentialsProvider.isPresent) {
                String.format(
                    wsUri,
                    credentialsProvider.get().getUsername(),
                    credentialsProvider.get().getPassword()
                )
            } else {
                wsUri
            }

            val socketFactory = createTlsSocketFactory(trustStore)
            val clientBuilder = OkHttpClient.Builder()
                .sslSocketFactory(Tls12SocketFactory(socketFactory.first()), socketFactory.second())
                .connectionSpecs(
                    serviceUrl.getConnectionSpecs()
                        .orElseGet { listOf(ConnectionSpec.RESTRICTED_TLS) })
                .readTimeout(KEEPALIVE_FREQUENCY_SECONDS + 10L, TimeUnit.SECONDS)
                .dns(dns.orElseGet { Dns.SYSTEM })
                .connectTimeout(KEEPALIVE_FREQUENCY_SECONDS + 10L, TimeUnit.SECONDS)

            for (interceptor in interceptors) {
                clientBuilder.addInterceptor(interceptor)
            }

            if (signalProxy.isPresent) {
                clientBuilder.socketFactory(
                    TlsProxySocketFactory(
                        signalProxy.get().host,
                        signalProxy.get().port,
                        dns
                    )
                )
            }

            val okHttpClient = clientBuilder.build()
            val requestBuilder = Request.Builder().url(filledUri)

            if (signalAgent != null) {
                requestBuilder.addHeader("X-Signal-Agent", signalAgent)
            }
            requestBuilder.addHeader(
                "X-Signal-Receive-Stories",
                if (allowStories) "true" else "false"
            )

            if (serviceUrl.hostHeader.isPresent) {
                requestBuilder.addHeader("Host", serviceUrl.hostHeader.get())
                Log.w(TAG, "Using alternate host: " + serviceUrl.hostHeader.get())
            }

            webSocketState.onNext(WebSocketConnectionState.CONNECTING)
            client = okHttpClient.newWebSocket(requestBuilder.build(), this)
        }
        return webSocketState
    }

    override fun isDead(): Boolean = lock.withLock {
        return client == null
    }

    override fun disconnect() = lock.withLock {
        log("disconnect()")
        client?.let {
            it.close(1000, "OK")
            client = null
            webSocketState.onNext(WebSocketConnectionState.DISCONNECTING)
        }
        condition.signalAll()
    }

    override fun sendRequest(request: WebSocketRequestMessage): Single<WebsocketResponse> {
        if (client == null) {
            throw IOException("No connection!")
        }

        val message = WebSocketMessage.Builder()
            .type(WebSocketMessage.Type.REQUEST)
            .request(request)
            .build()

        val single = SingleSubject.create<WebsocketResponse>()
        outgoingRequests[request.id!!] = OutgoingRequest(single)

        if (client?.send(message.encode().toByteString()) != true) {
            throw IOException("Write failed!")
        }

        return single.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .timeout(10, TimeUnit.SECONDS, Schedulers.io())
    }

    override fun sendKeepAlive() {
        if (client != null) {
            log("Sending keep alive...")

            val id = System.currentTimeMillis()
            val message = WebSocketMessage.Builder()
                .type(WebSocketMessage.Type.REQUEST)
                .request(
                    WebSocketRequestMessage.Builder()
                        .id(id)
                        .path("/v1/keepalive")
                        .verb("GET")
                        .build()
                ).build()
                .encode()

            keepAlives.add(id)
            if (client?.send(message.toByteString()) != true) {
                throw IOException("Write failed!")
            }
        }
    }

    override fun readRequestIfAvailable(): Optional<WebSocketRequestMessage> {
        if (incomingRequests.size > 0) {
            return Optional.of(incomingRequests.removeFirst())
        } else {
            return Optional.empty()
        }
    }

    @Throws(TimeoutException::class, IOException::class)
    override fun readRequest(timeoutMillis: Long): WebSocketRequestMessage {
        lock.withLock {
            if (client == null) {
                throw IOException("Connection closed!")
            }
            val startTime = System.currentTimeMillis()

            while (client != null && incomingRequests.isEmpty() && elapsedTime(startTime) < timeoutMillis) {
                Util.wait(lock, max(1L, (timeoutMillis - elapsedTime(startTime))))
            }

            if (incomingRequests.isEmpty() && client == null) {
                throw IOException("Connection closed!")
            } else if (incomingRequests.isEmpty()) {
                throw TimeoutException("Timeout exceeded")
            } else {
                return incomingRequests.removeFirst()
            }
        }
    }

    override fun sendResponse(response: WebSocketResponseMessage?) {
        if (client == null) {
            throw IOException("Connection closed!")
        }

        val message = WebSocketMessage.Builder()
            .type(WebSocketMessage.Type.RESPONSE)
            .response(response)
            .build()

        if (client?.send(message.encode().toByteString()) != true) {
            throw IOException("Write failed!")
        }
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        if (client != null) {
            log("onOpen() connected")
            webSocketState.onNext(WebSocketConnectionState.CONNECTED)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) = lock.withLock {
        try {
            val message = WebSocketMessage.ADAPTER.decode(bytes)
            when (message.type) {
                WebSocketMessage.Type.REQUEST -> {
                    message.request?.let {
                        incomingRequests.add(it)
                    }
                }

                WebSocketMessage.Type.RESPONSE -> {
                    val listener = outgoingRequests.remove(message.response?.id)
                    if (listener != null) {
                        listener.onSuccess(
                            WebsocketResponse(
                                message.response?.status,
                                message.response?.body?.toByteArray()?.toString() ?: "",
                                message.response?.headers ?: listOf(),
                                !credentialsProvider.isPresent
                            )
                        )
                        val status = message.response?.status
                        if (status != null && status > 400) {
                            healthMonitor.onMessageError(
                                message.response.status,
                                credentialsProvider.isPresent
                            )
                        }
                    } else if (keepAlives.remove(message.response?.id)) {
                        message.response?.id?.let {
                            healthMonitor.onKeepAliveResponse(it, credentialsProvider.isPresent)
                        }
                    }
                }

                else -> {}
            }

            condition.signalAll()
        } catch (e: IOException) {
            warn(e)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "onMessage(text)")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        warn("onFailure()", t)
        if (response != null && (response.code == 401 || response.code == 403)) {
            webSocketState.onNext(WebSocketConnectionState.AUTHENTICATION_FAILED)
        } else {
            webSocketState.onNext(WebSocketConnectionState.FAILED)
        }
        cleanupAfterShutdown()
        condition.signalAll()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        log("onClosing()")
        webSocketState.onNext(WebSocketConnectionState.DISCONNECTING)
        webSocket.close(1000, "OK")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = lock.withLock {
        log("onClose()")
        webSocketState.onNext(WebSocketConnectionState.DISCONNECTED)
        cleanupAfterShutdown()
        condition.signalAll()
    }

    private fun cleanupAfterShutdown() {
        val iterator = outgoingRequests.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.onError(IOException("Closed unexpectedly"))
            iterator.remove()
        }

        client?.let {
            log("Client not null when closed")
            it.close(1000, "OK")
            client = null
        }
    }

    private fun getConnectionInfo(): Pair<SignalServiceUrl, String> {
        val serviceUrl = serviceUrls[random.nextInt(serviceUrls.size)]
        val uri = serviceUrl.url.replace("https://", "wss://").replace("http://", "ws://")

        if (credentialsProvider.isPresent) {
            return Pair(serviceUrl, "$uri/v1/websocket/$extraPathUri?login=%s&password=%s")
        } else {
            return Pair(serviceUrl, "$uri/v1/websocket/$extraPathUri")
        }
    }

    private fun elapsedTime(startTime: Long): Long {
        return System.currentTimeMillis() - startTime
    }

    private fun createTlsSocketFactory(trustStore: TrustStore): Pair<SSLSocketFactory, X509TrustManager> {
        try {
            val context = SSLContext.getInstance("TLS")
            val trustManagers: Array<TrustManager> = BlacklistingTrustManager.createFor(trustStore)
            context.init(null, trustManagers, null)

            return Pair(context.socketFactory, trustManagers[0] as X509TrustManager)
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError(e)
        } catch (e: KeyManagementException) {
            throw AssertionError(e)
        }
    }

    private fun log(message: String) {
        Log.i(TAG, "$name $message")
    }

    private fun warn(message: String) {
        Log.w(TAG, "$name $message")
    }

    private fun warn(e: Throwable) {
        Log.w(TAG, name, e)
    }

    private fun warn(message: String, e: Throwable) {
        Log.w(TAG, "$name $message", e)
    }

    class OutgoingRequest(private val responseSingle: SingleSubject<WebsocketResponse>) {

        fun onSuccess(response: WebsocketResponse) {
            responseSingle.onSuccess(response)
        }

        fun onError(throwable: Throwable) {
            responseSingle.onError(throwable)
        }
    }

    companion object {
        private val TAG: String = OkHttpWebSocketConnection::class.java.simpleName
        const val KEEPALIVE_FREQUENCY_SECONDS: Int = 30
    }

}