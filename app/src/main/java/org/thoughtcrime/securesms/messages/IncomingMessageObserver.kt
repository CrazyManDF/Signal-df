package org.thoughtcrime.securesms.messages

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.collections.immutable.toImmutableSet
import okhttp3.internal.toImmutableList
import  java.util.concurrent.Semaphore
import org.signal.core.util.ThreadUtil
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.crypto.ReentrantSessionLock
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.groups.GroupsV2ProcessingLock
import org.thoughtcrime.securesms.jobmanager.impl.BackoffUtil
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraint
import org.thoughtcrime.securesms.jobs.ForegroundServiceUtil
import org.thoughtcrime.securesms.jobs.ForegroundServiceUtil.startWhenCapable
import org.thoughtcrime.securesms.jobs.PushProcessMessageJob
import org.thoughtcrime.securesms.jobs.UnableToStartException
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.messages.protocol.BufferedProtocolStore
import org.thoughtcrime.securesms.notifications.NotificationChannels
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.AppForegroundObserver
import org.thoughtcrime.securesms.util.SignalLocalMetrics
import org.whispersystems.signalservice.api.SignalWebSocket
import org.whispersystems.signalservice.api.messages.EnvelopeResponse
import org.whispersystems.signalservice.api.push.ServiceId
import org.whispersystems.signalservice.api.websocket.WebSocketUnavailableException
import org.whispersystems.signalservice.internal.push.Envelope
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.withLock
import kotlin.math.round
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.minutes

class IncomingMessageObserver(private val context: Application) {

    companion object {
        private val TAG = Log.tag(IncomingMessageObserver::class.java)

        private val websocketReadTimeout: Long
            get() = if (censored) 30.seconds.inWholeMilliseconds else 1.minutes.inWholeMilliseconds

        private val keepAliveTokenMaxAge: Long
            get() = if (censored) 2.minutes.inWholeMilliseconds else 5.minutes.inWholeMilliseconds

        private val maxBackgroundTime: Long
            get() = if (censored) 10.seconds.inWholeMilliseconds else 2.minutes.inWholeMilliseconds

        private val INSTANCE_COUNT = AtomicInteger(0)

        const val FOREGROUND_ID = 313399

        private val censored: Boolean
            get() = ApplicationDependencies.getSignalServiceNetworkAccess().isCensored()
    }

    private val decryptionDrainedListeners: MutableList<Runnable> = CopyOnWriteArrayList()
    private val keepAliveTokens: MutableMap<String, Long> = mutableMapOf()
    private val keepAlivePurgeCallbacks: MutableMap<String, MutableList<Runnable>> = mutableMapOf()

    private val lock: ReentrantLock = ReentrantLock()
    private val connectionNecessarySemaphore = Semaphore(0)


    private var appVisible = false
    private var lastInteractionTime: Long = System.currentTimeMillis()

    @Volatile
    private var terminated = false

    @Volatile
    var decryptionDrained = false
        private set

    init {
        if (INSTANCE_COUNT.incrementAndGet() != 1) {
            throw AssertionError("Multiple observers!")
        }

        MessageRetrievalThread().start()

        if (!SignalStore.account().fcmEnabled || SignalStore.internalValues().isWebsocketModeForced) {
            try {
                ForegroundServiceUtil.start(context, Intent(context, ForegroundService::class.java))
            } catch (e: UnableToStartException) {
                Log.w(
                    TAG,
                    "Unable to start foreground service for websocket. Deferring to background to try with blocking"
                )
                SignalExecutors.UNBOUNDED.execute {
                    try {
                        startWhenCapable(context, Intent(context, ForegroundService::class.java))
                    } catch (e: UnableToStartException) {
                        Log.w(TAG, "Unable to start foreground service for websocket!", e)
                    }
                }
            }
        }

        ApplicationDependencies.appForegroundObserver?.addListener(object :
            AppForegroundObserver.Listener {

            override fun onForeground() {
                onAppForegrounded()
            }

            override fun onBackground() {
                onAppBackgrounded()
            }
        })
    }

    private fun disconnect() {
        ApplicationDependencies.signalWebSocket.disconnect()
    }

    fun registerKeepAliveToken(key: String, runnable: Runnable? = null) {
        lock.withLock {
            keepAliveTokens[key] = System.currentTimeMillis()
            if (runnable != null) {
                if (!keepAlivePurgeCallbacks.containsKey(key)) {
                    keepAlivePurgeCallbacks[key] = ArrayList()
                }
                keepAlivePurgeCallbacks[key]?.add(runnable)
            }
        }
        lastInteractionTime = System.currentTimeMillis()
//        connectionNecessarySemaphore.release()
    }

    fun addDecryptionDrainedListener(listener: Runnable) {
        decryptionDrainedListeners.add(listener)
        if (decryptionDrained) {
            listener.run()
        }
    }

    fun removeDecryptionDrainedListener(listener: Runnable) {
        decryptionDrainedListeners.remove(listener)
    }

    fun removeKeepAliveToken(key: String) {
        lock.withLock {
            keepAliveTokens.remove(key)
            keepAlivePurgeCallbacks.remove(key)
            lastInteractionTime = System.currentTimeMillis()
            connectionNecessarySemaphore.release()
        }
    }

    private fun onAppForegrounded() {
        lock.withLock {
            appVisible = true
            BackgroundService.start(context)
            connectionNecessarySemaphore.release()
        }
    }

    private fun onAppBackgrounded() {
        lock.withLock {
            appVisible = false
            lastInteractionTime = System.currentTimeMillis()
            connectionNecessarySemaphore.release()
        }
    }

    private fun isConnectionNecessary(): Boolean {
        val timeIdle: Long
        val keepAliveEntries: Set<Pair<String, Long>>
        val appVisibleSnapshot: Boolean

        lock.withLock {
            appVisibleSnapshot = appVisible
            timeIdle =
                if (appVisibleSnapshot) 0L else (System.currentTimeMillis() - lastInteractionTime)

            val keepAliveCutoffTime = System.currentTimeMillis() - keepAliveTokenMaxAge
            keepAliveEntries = keepAliveTokens.entries.mapNotNull { (key, createTime) ->
                if (createTime < keepAliveCutoffTime) {
                    Log.d(TAG, "Removed old keep web socket keep alive token $key")
                    keepAlivePurgeCallbacks.remove(key)?.forEach { it.run() }
                    null
                } else {
                    key to createTime
                }
            }.toImmutableSet()
        }

        val registered = SignalStore.account().isRegistered
        val fcmEnabled = SignalStore.account().fcmEnabled
        val hasNetwork = NetworkConstraint.isMet(context)
        val hasProxy = SignalStore.proxy().isProxyEnabled()
        val forceWebsocket = SignalStore.internalValues().isWebsocketModeForced

        val lastInteractionString =
            if (appVisibleSnapshot) "N/A" else timeIdle.toString() + " ms (" + (if (timeIdle < maxBackgroundTime) "within limit" else "over limit") + ")"
        val conclusion = registered &&
                (appVisibleSnapshot || timeIdle < maxBackgroundTime || !fcmEnabled || keepAliveEntries.isNotEmpty()) &&
                hasNetwork

        val needsConnectionString =
            if (conclusion) "Needs Connection" else "Does Not Need Connection"

        Log.d(
            TAG,
            "[$needsConnectionString] Network: $hasNetwork, Foreground: $appVisibleSnapshot, Time Since Last Interaction: $lastInteractionString, FCM: $fcmEnabled, Stay open requests: $keepAliveEntries, Registered: $registered, Proxy: $hasProxy, Force websocket: $forceWebsocket"
        )
        return conclusion
    }

    private inner class MessageRetrievalThread : Thread("MessageRetrievalService"),
        Thread.UncaughtExceptionHandler {

        init {
            Log.i(TAG, "Initializing! (${this.hashCode()})")
            uncaughtExceptionHandler = this
        }

        override fun run() {
            var attempts = 0

            while (!terminated) {
                Log.i(TAG, "Waiting for websocket state change....")

                if (attempts > 1) {
                    val backoff =
                        BackoffUtil.exponentialBackoff(attempts, TimeUnit.SECONDS.toMillis(30))
                    Log.w(
                        TAG,
                        "Too many failed connection attempts,  attempts: $attempts backing off: $backoff"
                    )
                    ThreadUtil.sleep(backoff)
                }

                waitForConnectionNecessary()
                Log.i(TAG, "Making websocket connection....")

                val signalWebSocket: SignalWebSocket = ApplicationDependencies.signalWebSocket
                val webSocketDisposable = signalWebSocket.webSocketState.subscribe { state ->
                    Log.d(TAG, "WebSocket State: $state")
                    decryptionDrained = false
                }

                signalWebSocket.connect()

                try {
                    while (isConnectionNecessary()) {
                        try {
                            Log.d(TAG, "Reading message...")

                            val hasMore = signalWebSocket.readMessageBatch(websocketReadTimeout, 30,
                                object : SignalWebSocket.MessageReceivedCallback {
                                    override fun onMessageBatch(envelopeResponses: List<EnvelopeResponse>) {
                                        Log.i(TAG, "Retrieved ${envelopeResponses.size} envelopes!")
                                        val bufferedStore = BufferedProtocolStore.create()

                                        val startTime = System.currentTimeMillis()
                                        GroupsV2ProcessingLock.acquireGroupProcessingLock().use {
                                            ReentrantSessionLock.INSTANCE.acquire().use {
                                                envelopeResponses.forEach { response ->
                                                    Log.d(TAG, "Beginning database transaction...")
                                                    val followUpOperations =
                                                        SignalDatabase.runInTransaction { db ->
                                                            val followUps = processEnvelope(
                                                                bufferedStore,
                                                                response.envelope,
                                                                response.serverDeliveredTimestamp
                                                            )
                                                            bufferedStore.flushToDisk()
                                                            followUps
                                                        }
                                                    Log.d(TAG, "Ended database transaction.")

                                                    if (followUpOperations != null) {
                                                        Log.d(
                                                            TAG,
                                                            "Running ${followUpOperations.size} follow-up operations..."
                                                        )
                                                        val jobs =
                                                            followUpOperations.mapNotNull { it.run() }
                                                        ApplicationDependencies.jobManager.addAllChains(
                                                            jobs
                                                        )
                                                    }
                                                    signalWebSocket.sendAck(response)
                                                }
                                            }
                                        }
                                        val duration = System.currentTimeMillis() - startTime
                                        val timePerMessage: Float =
                                            duration / envelopeResponses.size.toFloat()
                                        Log.d(
                                            TAG,
                                            "Decrypted ${envelopeResponses.size} envelopes in $duration ms (~${
                                                round(timePerMessage * 100) / 100
                                            } ms per message)"
                                        )
                                    }
                                }
                            )
                            attempts = 0
                            SignalLocalMetrics.PushWebsocketFetch.onProcessedBatch()

                            if (!hasMore && !decryptionDrained) {
                                Log.i(TAG, "Decryptions newly-drained.")
                                decryptionDrained = true

                                for (listener in decryptionDrainedListeners.toList()) {
                                    listener.run()
                                }
                            } else if (!hasMore) {
                                Log.w(
                                    TAG,
                                    "Got tombstone, but we thought the network was already drained!"
                                )
                            }
                        } catch (e: WebSocketUnavailableException) {
                            Log.i(TAG, "Pipe unexpectedly unavailable, connecting")
                            signalWebSocket.connect()
                        } catch (e: TimeoutException) {
                            Log.w(TAG, "Application level read timeout...")
                            attempts = 0
                        }
                    }

                    if (!appVisible) {
                        BackgroundService.stop(context)
                    }

                } catch (e: Throwable) {
                    attempts++
                    Log.w(TAG, e)
                    e.printStackTrace()
                } finally {
                    Log.w(TAG, "Shutting down pipe...")
                    disconnect()
                    webSocketDisposable.dispose()
                }
                Log.i(TAG, "Looping...")
            }
            Log.w(TAG, "Terminated! (${this.hashCode()})")
        }

        override fun uncaughtException(t: Thread, e: Throwable) {
            Log.w(TAG, "Uncaught exception in message thread!", e)
        }
    }

    fun processEnvelope(
        bufferedProtocolStore: BufferedProtocolStore,
        envelope: Envelope,
        serverDeliveredTimestamp: Long
    ): List<MessageDecryptor.FollowUpOperation>? {
        return when (envelope.type) {
            Envelope.Type.RECEIPT -> {
                processReceipt(envelope)
                null
            }

            Envelope.Type.PREKEY_BUNDLE,
            Envelope.Type.CIPHERTEXT,
            Envelope.Type.UNIDENTIFIED_SENDER,
            Envelope.Type.PLAINTEXT_CONTENT -> {
                processMessage(bufferedProtocolStore, envelope, serverDeliveredTimestamp)
            }

            else -> {
                Log.w(TAG, "Received envelope of unknown type: " + envelope.type)
                null
            }
        }
    }

    private fun processMessage(
        bufferedProtocolStore: BufferedProtocolStore,
        envelope: Envelope,
        serverDeliveredTimestamp: Long
    ): List<MessageDecryptor.FollowUpOperation> {
        val localReceiveMetric = SignalLocalMetrics.MessageReceive.start()
        val result = MessageDecryptor.decrypt(context, bufferedProtocolStore, envelope, serverDeliveredTimestamp)
        localReceiveMetric.onEnvelopeDecrypted()

        SignalLocalMetrics.MessageLatency.onMessageReceived(envelope.serverTimestamp!!, serverDeliveredTimestamp, envelope.urgent!!)
        when (result) {
            is MessageDecryptor.Result.Success -> {
                val job = PushProcessMessageJob.processOrDefer(messageContentProcessor, result, localReceiveMetric)
                if (job != null) {
                    return result.followUpOperations + FollowUpOperation { job.asChain() }
                }
            }
            is MessageDecryptor.Result.Error -> {
                return result.followUpOperations + MessageDecryptor.FollowUpOperation {
                    PushProcessMessageErrorJob(
                        result.toMessageState(),
                        result.errorMetadata.toExceptionMetadata(),
                        result.envelope.timestamp!!
                    ).asChain()
                }
            }
            is MessageDecryptor.Result.Ignore -> {
                // No action needed
            }
            else -> {
                throw AssertionError("Unexpected result! ${result.javaClass.simpleName}")
            }
        }
        return result.followUpOperations
    }

    private fun processReceipt(envelope: Envelope) {
        val serviceId = ServiceId.parseOrNull(envelope.sourceServiceId)
        if (serviceId == null) {
            Log.w(TAG, "Invalid envelope sourceServiceId!")
            return
        }

        val senderId = RecipientId.from(serviceId)

        Log.i(
            TAG,
            "Received server receipt. Sender: $senderId, Device: ${envelope.sourceDevice}, Timestamp: ${envelope.timestamp}"
        )
        SignalDatabase.messages.incrementDeliveryReceiptCount(
            envelope.timestamp!!,
            senderId,
            System.currentTimeMillis()
        )
//        SignalDatabase.messageLog.deleteEntryForRecipient(
//            envelope.timestamp!!,
//            senderId,
//            envelope.sourceDevice!!
        )
    }

    private fun waitForConnectionNecessary() {
        try {
            connectionNecessarySemaphore.drainPermits()
            while (!isConnectionNecessary()) {
                val numberDrained = connectionNecessarySemaphore.drainPermits()
                if (numberDrained == 0) {
                    connectionNecessarySemaphore.acquire()
                }
            }
        } catch (e: InterruptedException) {
            throw AssertionError(e)
        }
    }

    class ForegroundService : Service() {
        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onCreate() {
            postForegroundNotification()
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            postForegroundNotification()
            return START_STICKY
        }

        private fun postForegroundNotification() {
            val notification = NotificationCompat.Builder(
                applicationContext,
                NotificationChannels.instance.BACKGROUND
            )
                .setContentTitle(applicationContext.getString(R.string.MessageRetrievalService_signal))
                .setContentText(applicationContext.getString(R.string.MessageRetrievalService_background_connection_enabled))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setWhen(0)
                .setSmallIcon(R.drawable.ic_signal_background_connection)
                .build()

            startForeground(FOREGROUND_ID, notification)
        }
    }

    class BackgroundService : Service() {
        override fun onBind(intent: Intent?): IBinder? = null

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            Log.d(TAG, "Background service started.")
            return START_STICKY
        }

        override fun onDestroy() {
            Log.d(TAG, "Background service destroyed.")
        }

        companion object {
            fun start(context: Context) {
                try {
                    context.startService(Intent(context, BackgroundService::class.java))
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to start background service.", e)
                }
            }

            fun stop(context: Context) {
                context.stopService(Intent(context, BackgroundService::class.java))
            }
        }
    }
}