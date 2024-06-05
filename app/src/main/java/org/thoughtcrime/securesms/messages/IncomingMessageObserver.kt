package org.thoughtcrime.securesms.messages

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import  java.util.concurrent.Semaphore
import org.signal.core.util.ThreadUtil
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.jobmanager.impl.BackoffUtil
import org.thoughtcrime.securesms.jobs.ForegroundServiceUtil
import org.thoughtcrime.securesms.jobs.ForegroundServiceUtil.startWhenCapable
import org.thoughtcrime.securesms.jobs.UnableToStartException
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.notifications.NotificationChannels
import org.thoughtcrime.securesms.util.AppForegroundObserver
import org.whispersystems.signalservice.api.SignalWebSocket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.withLock
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
        if (INSTANCE_COUNT.incrementAndGet() != 1){
            throw AssertionError("Multiple observers!")
        }

        MessageRetrievalThread().start()

        if (!SignalStore.account().fcmEnabled || SignalStore.internalValues().isWebsocketModeForced) {
            try {
                ForegroundServiceUtil.start(context, Intent(context, ForegroundService::class.java))
            } catch (e: UnableToStartException) {
                Log.w(TAG, "Unable to start foreground service for websocket. Deferring to background to try with blocking")
                SignalExecutors.UNBOUNDED.execute {
                    try {
                        startWhenCapable(context, Intent(context, ForegroundService::class.java))
                    } catch (e: UnableToStartException) {
                        Log.w(TAG, "Unable to start foreground service for websocket!", e)
                    }
                }
            }
        }

        ApplicationDependencies.appForegroundObserver?.addListener(object : AppForegroundObserver.Listener {

            override fun onForeground() {
                onAppForegrounded()
            }

            override fun onBackground() {
                onAppBackgrounded()
            }
        })

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

    private inner class MessageRetrievalThread : Thread("MessageRetrievalService"), Thread.UncaughtExceptionHandler{

        init {
            Log.i(TAG, "Initializing! (${this.hashCode()})")
            uncaughtExceptionHandler = this
        }

        override fun run() {
            var attempts = 0

            while (!terminated){
                Log.i(TAG, "Waiting for websocket state change....")

                if (attempts > 1){
                    val backoff = BackoffUtil.exponentialBackoff(attempts, TimeUnit.SECONDS.toMillis(30))
                    Log.w(TAG, "Too many failed connection attempts,  attempts: $attempts backing off: $backoff")
                    ThreadUtil.sleep(backoff)
                }

                waitForConnectionNecessary()
                Log.i(TAG, "Making websocket connection....")

                val signalWebSocket: SignalWebSocket = ApplicationDependencies.signalWebSocket
                val webSocketDisposable = signalWebSocket.webSocketState.subscribe {state ->
                    Log.d(TAG, "WebSocket State: $state")
                    decryptionDrained = false
                }

                signalWebSocket.connect()

                // TODO:
            }

            Log.w(TAG, "Terminated! (${this.hashCode()})")
        }

        override fun uncaughtException(t: Thread, e: Throwable) {
            Log.w(TAG, "Uncaught exception in message thread!", e)
        }
    }

    private fun waitForConnectionNecessary() {
        try {
            connectionNecessarySemaphore.drainPermits()
            while (!isConnectionNecessary()){
                val numberDrained = connectionNecessarySemaphore.drainPermits()
                if (numberDrained == 0) {
                    connectionNecessarySemaphore.acquire()
                }
            }
        }catch (e: InterruptedException){
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
            val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.instance.BACKGROUND )
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