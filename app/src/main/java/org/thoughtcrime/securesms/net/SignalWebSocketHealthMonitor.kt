package org.thoughtcrime.securesms.net

import android.app.Application
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.util.TextSecurePreferences
import org.whispersystems.signalservice.api.SignalWebSocket
import org.whispersystems.signalservice.api.util.Preconditions
import org.whispersystems.signalservice.api.util.SleepTimer
import org.whispersystems.signalservice.api.websocket.HealthMonitor
import org.whispersystems.signalservice.api.websocket.WebSocketConnectionState
import org.whispersystems.signalservice.internal.websocket.OkHttpWebSocketConnection
import java.util.concurrent.Executors
import kotlin.math.sign
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SignalWebSocketHealthMonitor(
    private val context: Application,
    private val sleepTimer: SleepTimer
) : HealthMonitor {

    private val executor = Executors.newSingleThreadExecutor()
    private var signalWebSocket: SignalWebSocket? = null
    private var keepAliveSender: KeepAliveSender? = null

    private val identified = HealthState()
    private val unidentified = HealthState()

    fun monitor(signalWebSocket: SignalWebSocket?) {
        executor.execute {

            Preconditions.checkNotNull(signalWebSocket)
            Preconditions.checkArgument(signalWebSocket == null, "monitor can only be called once")

            val a = signalWebSocket!!.webSocketState
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .distinctUntilChanged()
                .subscribe { onStateChange(it, identified, true) }

            val b = signalWebSocket.unidentifiedWebSocketState
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .distinctUntilChanged()
                .subscribe { onStateChange(it, unidentified, false) }

        }
    }

    private fun onStateChange(
        connectionState: WebSocketConnectionState?,
        healthState: HealthState,
        isIdentified: Boolean
    ) {
        executor.execute {
            when (connectionState) {
                WebSocketConnectionState.CONNECTED -> {
                    if (isIdentified) {
                        TextSecurePreferences.setUnauthorizedReceived(context, false)
                    }
                }

                WebSocketConnectionState.AUTHENTICATION_FAILED -> {
                    if (isIdentified) {
                        TextSecurePreferences.setUnauthorizedReceived(context, true)
                    }
                }

                WebSocketConnectionState.FAILED -> {

                }

                else -> {}
            }

            healthState.needsKeepAlive = connectionState == WebSocketConnectionState.CONNECTED
            if (keepAliveSender == null && isKeepAliveNecessary()) {
                keepAliveSender = KeepAliveSender()
                keepAliveSender?.start()
            } else if (keepAliveSender != null && !isKeepAliveNecessary()) {
                keepAliveSender?.shutdown()
                keepAliveSender = null
            }
        }
    }

    override fun onKeepAliveResponse(sentTimestamp: Long, isIdentifiedWebSocket: Boolean) {

    }

    override fun onMessageError(status: Int, isIdentifiedWebSocket: Boolean) {
        executor.execute {
            if (status == 409) {
                val healthState = if (isIdentifiedWebSocket) identified else unidentified
                if (healthState.mismatchErrorTracker.addSample(System.currentTimeMillis())) {
                    Log.w(TAG, "Received too many mismatch device errors, forcing new websockets.")
                    signalWebSocket?.forceNewWebSockets()
                }
            }
        }
    }

    private fun isKeepAliveNecessary(): Boolean {
        return identified.needsKeepAlive || unidentified.needsKeepAlive
    }

    private class HealthState {

        val mismatchErrorTracker = HttpErrorTracker(5, 1.minutes.inWholeMilliseconds)

        var needsKeepAlive: Boolean = false

        var lastKeepAliveReceived: Long = 0L
    }

    private inner class KeepAliveSender : Thread() {

        @Volatile
        private var shouldKeepRunning = true
        override fun run() {
            identified.lastKeepAliveReceived = System.currentTimeMillis()
            unidentified.lastKeepAliveReceived = System.currentTimeMillis()

            var keepAliveSendTime = System.currentTimeMillis()
            while (shouldKeepRunning && isKeepAliveNecessary()) {
                try {
                    val nextKeepAliveSendTime = keepAliveSendTime + KEEP_ALIVE_SEND_CADENCE
                    sleepUntil(nextKeepAliveSendTime)

                    if (shouldKeepRunning && isKeepAliveNecessary()) {
                        keepAliveSendTime = System.currentTimeMillis()
                        signalWebSocket?.sendKeepAlive()
                    }

                    val responseRequiredTime = keepAliveSendTime + KEEP_ALIVE_TIMEOUT
                    sleepUntil(responseRequiredTime)

                    if (shouldKeepRunning && isKeepAliveNecessary()) {
                        if (identified.lastKeepAliveReceived < keepAliveSendTime || unidentified.lastKeepAliveReceived < keepAliveSendTime) {
                            Log.w(
                                TAG,
                                "Missed keep alives, identified last: " + identified.lastKeepAliveReceived +
                                        " unidentified last: " + unidentified.lastKeepAliveReceived +
                                        " needed by: " + responseRequiredTime
                            )
                            signalWebSocket?.forceNewWebSockets()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    Log.w(TAG, e)
                }
            }
        }

        private fun sleepUntil(nextKeepAliveSendTime: Long) {
            while (System.currentTimeMillis() < nextKeepAliveSendTime) {
                val waitTime = nextKeepAliveSendTime - System.currentTimeMillis()
                if (waitTime > 0) {
                    try {
                        sleepTimer.sleep(waitTime)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        Log.w(TAG, e)
                    }
                }

            }
        }

        fun shutdown() {
            shouldKeepRunning = false
        }
    }

    companion object {
        private val TAG = Log.tag(SignalWebSocketHealthMonitor::class.java)

        val KEEP_ALIVE_SEND_CADENCE =
            OkHttpWebSocketConnection.KEEPALIVE_FREQUENCY_SECONDS.seconds.inWholeMilliseconds
        val KEEP_ALIVE_TIMEOUT = 20.seconds.inWholeMilliseconds
    }
}