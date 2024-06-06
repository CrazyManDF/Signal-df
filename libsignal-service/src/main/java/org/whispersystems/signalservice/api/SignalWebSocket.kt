package org.whispersystems.signalservice.api

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.signal.libsignal.protocol.logging.Log
import org.whispersystems.signalservice.api.messages.EnvelopeResponse
import org.whispersystems.signalservice.api.websocket.WebSocketConnectionState
import org.whispersystems.signalservice.api.websocket.WebSocketFactory
import org.whispersystems.signalservice.api.websocket.WebSocketUnavailableException
import org.whispersystems.signalservice.internal.push.Envelope
import org.whispersystems.signalservice.internal.websocket.WebSocketConnection
import org.whispersystems.signalservice.internal.websocket.WebSocketRequestMessage
import org.whispersystems.signalservice.internal.websocket.WebSocketResponseMessage
import org.whispersystems.signalservice.internal.websocket.WebsocketResponse
import java.io.IOException
import java.util.Locale
import java.util.Optional
import java.util.concurrent.TimeoutException

class SignalWebSocket(private val webSocketFactory: WebSocketFactory) {

    val webSocketState: BehaviorSubject<WebSocketConnectionState> =
        BehaviorSubject.createDefault(WebSocketConnectionState.DISCONNECTED)
    val unidentifiedWebSocketState: BehaviorSubject<WebSocketConnectionState> =
        BehaviorSubject.createDefault(WebSocketConnectionState.DISCONNECTED)

    var webSocketStateDisposable: CompositeDisposable = CompositeDisposable()
    var unidentifiedWebSocketStateDisposable: CompositeDisposable = CompositeDisposable()

    private var canConnect = false

    companion object {
        private val TAG: String = SignalWebSocket::class.java.simpleName

        private const val SERVER_DELIVERED_TIMESTAMP_HEADER = "X-Signal-Timestamp"
    }

    @Synchronized
    fun connect() {
        canConnect = true
        try {
            webSocket
            unidentifiedWebSocket
        } catch (e: WebSocketUnavailableException) {
            throw AssertionError(e)
        }
    }

    @Synchronized
    fun disconnect() {
        canConnect = false
        disconnectIdentified()
        disconnectUnidentified()
    }

    @Synchronized
    fun forceNewWebSockets() {
        Log.i(
            TAG, "Forcing new WebSockets " +
                    " identified: " + (if (webSocket != null) webSocket!!.name else "[null]") +
                    " unidentified: " + (if (unidentifiedWebSocket != null) unidentifiedWebSocket!!.name else "[null]") +
                    " canConnect: " + canConnect
        )

        disconnectIdentified()
        disconnectUnidentified()
    }

    private fun disconnectIdentified() {
        webSocket?.let {
            webSocketStateDisposable.dispose()

            it.disconnect()
            webSocket = null

            if (webSocketState.value?.isFailure() != true) {
                webSocketState.onNext(WebSocketConnectionState.DISCONNECTED)
            }
        }
    }

    private fun disconnectUnidentified() {
        unidentifiedWebSocket?.let {
            unidentifiedWebSocketStateDisposable.dispose()

            it.disconnect()
            unidentifiedWebSocket = null

            if (unidentifiedWebSocketState.value?.isFailure() != true) {
                unidentifiedWebSocketState.onNext(WebSocketConnectionState.DISCONNECTED)
            }
        }
    }

    private var webSocket: WebSocketConnection? = null
        @Throws(WebSocketUnavailableException::class)
        @Synchronized
        get() {
            if (!canConnect) {
                throw WebSocketUnavailableException()
            }

            if (field == null || field?.isDead() == true) {
                webSocketStateDisposable.dispose()

                webSocketStateDisposable = CompositeDisposable()

                field = webSocketFactory.createWebSocket().apply {
                    val state = connect()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .subscribe(webSocketState::onNext)

                    webSocketStateDisposable.add(state)
                }
            }
            return field
        }

    private var unidentifiedWebSocket: WebSocketConnection? = null
        @Throws(WebSocketUnavailableException::class)
        @Synchronized
        get() {
            if (!canConnect) {
                throw WebSocketUnavailableException()
            }

            if (field == null || field?.isDead() == true) {
                unidentifiedWebSocketStateDisposable.dispose()

                unidentifiedWebSocketStateDisposable = CompositeDisposable()

                field = webSocketFactory.createWebSocket().apply {
                    val state = connect()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .subscribe(unidentifiedWebSocketState::onNext)

                    unidentifiedWebSocketStateDisposable.add(state)
                }
            }
            return field
        }

    @Throws(IOException::class)
    fun sendKeepAlive() {
        if (canConnect) {
            try {
                webSocket!!.sendKeepAlive()
                unidentifiedWebSocket?.sendKeepAlive()
            } catch (e: WebSocketUnavailableException) {
                throw AssertionError(e)
            }
        }
    }

    fun request(requestMessage: WebSocketRequestMessage): Single<WebsocketResponse> {
        return try {
            webSocket!!.sendRequest(requestMessage)
        } catch (e: IOException) {
            Single.error(e)
        }
    }

//    fun request(requestMessage: WebSocketRequestMessage, unidentifiedAccess: Optional<UnidentifiedAccess>): Single<WebsocketResponse> {
//
//    }

    @Throws(TimeoutException::class, WebSocketUnavailableException::class, IOException::class)
    fun readMessageBatch(
        timeout: Long,
        batchSize: Int,
        callback: MessageReceivedCallback
    ): Boolean {
        val responses = arrayListOf<EnvelopeResponse>()
        var hitEndOfQueue = false

        val firstEnvelope: Optional<EnvelopeResponse> = waitForSingleMessage(timeout)

        if (firstEnvelope.isPresent) {
            responses.add(firstEnvelope.get())
        } else {
            hitEndOfQueue = true
        }

        if (!hitEndOfQueue) {
            for (i in 1 until batchSize) {
                val request = webSocket!!.readRequestIfAvailable()

                if (request.isPresent) {
                    if (isSignalServiceEnvelope(request.get())) {
                        responses.add(requestToEnvelopeResponse(request.get()))
                    } else if (isSocketEmptyRequest(request.get())) {
                        hitEndOfQueue = true
                        break
                    }
                } else {
                    break
                }
            }
        }

        if (responses.isNotEmpty()) {
            callback.onMessageBatch(responses)
        }
        return !hitEndOfQueue
    }

    @Throws(IOException::class)
    fun sendAck(response: EnvelopeResponse) {
        webSocket!!.sendResponse(createWebSocketResponse(response.websocketRequest))
    }

    @Throws(TimeoutException::class, WebSocketUnavailableException::class, IOException::class)
    private fun waitForSingleMessage(timeout: Long): Optional<EnvelopeResponse> {
        while (true) {
            val request = webSocket!!.readRequest(timeout)
            if (isSignalServiceEnvelope((request))) {
                return Optional.of(requestToEnvelopeResponse(request))
            } else if (isSocketEmptyRequest(request)) {
                return Optional.empty()
            }
        }
    }

    @Throws(IOException::class)
    private fun requestToEnvelopeResponse(request: WebSocketRequestMessage): EnvelopeResponse {
        val timestampHeader: Optional<String> = findHeader(request)
        var timestamp = 0L

        if (timestampHeader.isPresent) {
            try {
                timestamp = timestampHeader.get().toLong()
            } catch (e: NumberFormatException) {
                Log.w(TAG, "Failed to parse $SERVER_DELIVERED_TIMESTAMP_HEADER")
            }
        }

        val envelope = Envelope.ADAPTER.decode(request.body?.toByteArray() ?: "".toByteArray())
        return EnvelopeResponse(envelope, timestamp, request)
    }

    private fun isSignalServiceEnvelope(message: WebSocketRequestMessage): Boolean {
        return "PUT" == message.verb && "/api/v1/message" == message.path
    }

    private fun isSocketEmptyRequest(message: WebSocketRequestMessage): Boolean {
        return "PUT" == message.verb && "/api/v1/queue/empty" == message.path
    }

    fun createWebSocketResponse(request: WebSocketRequestMessage): WebSocketResponseMessage {
       return if (isSignalServiceEnvelope(request)){
            WebSocketResponseMessage.Builder()
                .id(request.id)
                .status(200)
                .message("OK")
                .build()

        } else {
            WebSocketResponseMessage.Builder()
                .id(request.id)
                .status(400)
                .message("Unknown")
                .build();
        }
    }

    private fun findHeader(message: WebSocketRequestMessage): Optional<String> {
        if (message.headers.isEmpty()) {
            return Optional.empty()
        }

        for (header in message.headers) {
            if (header.startsWith(SERVER_DELIVERED_TIMESTAMP_HEADER)) {
                val split = header.split(":")
                if (split.size == 2 && split[0].trim().lowercase(Locale.getDefault())
                    == SERVER_DELIVERED_TIMESTAMP_HEADER.lowercase(Locale.getDefault())
                ) {
                    return Optional.of(split[1].trim())
                }
            }
        }

        return Optional.empty()
    }

    interface MessageReceivedCallback {
        fun onMessageBatch(envelopeResponses: List<EnvelopeResponse>)
    }
}