package org.thoughtcrime.securesms.jobs

import android.content.Context
import android.os.Build
import androidx.lifecycle.AtomicReference
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraint
import org.thoughtcrime.securesms.messages.WebSocketDrainer
import org.thoughtcrime.securesms.notifications.NotificationChannels
import org.thoughtcrime.securesms.service.GenericForegroundService
import org.thoughtcrime.securesms.util.AppForegroundObserver
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException
import java.io.Closeable
import java.io.IOException
import kotlin.concurrent.Volatile

class MessageFetchJob(parameters: Parameters) : BaseJob(parameters) {

    constructor() : this(
        Parameters.Builder()
            .addConstraint(NetworkConstraint.KEY)
            .setQueue("__notification_received")
            .setMaxAttempts(3)
            .setMaxInstancesForFactory(1)
            .build()
    )

    companion object {

        private val TAG = Log.tag(MessageFetchJob::class.java)

        const val KEY: String = "PushNotificationReceiveJob"
    }

    @Throws(IOException::class)
    override fun onRun() {
        runCatching {
            val controller = ForegroundServiceController.create(context!!)
            controller.awaitResult()
        }.onSuccess {
            Log.i(TAG, "Successfully pulled messages.")
        }.onFailure {
            throw PushNetworkException("Failed to pull messages.")
        }
    }

    override fun onShouldRetry(e: Exception): Boolean {
        Log.w(TAG, e)
        return e is PushNetworkException
    }

    override fun serialize(): ByteArray? {
        return null
    }

    override fun getFactoryKey(): String {
        return KEY
    }

    override fun onFailure() {

    }

    class ForegroundServiceController(private val context: Context) : AppForegroundObserver.Listener, Closeable {

        private val notificationController = AtomicReference<AutoCloseable>()

        @Volatile
        private var isRunning = false

        override fun onForeground() {
            if (!isRunning) {
                return
            }

            closeNotificationController()
        }

        override fun onBackground() {
            if (!isRunning) {
                return
            }

            if (notificationController.get() != null) {
                Log.w(TAG, "Already displaying or displayed a foreground notification.")
                return
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                notificationController.set(
                    GenericForegroundService.startForegroundTaskDelayed(
                        context,
                        context.getString(R.string.BackgroundMessageRetriever_checking_for_messages),
                        300,
                        R.drawable.ic_signal_refresh
                    )
                )
            } else {
                runCatching {
                    notificationController.set(
                        GenericForegroundService.startForegroundTask(
                            context,
                            context.getString(R.string.BackgroundMessageRetriever_checking_for_messages),
                            NotificationChannels.instance.OTHER,
                            R.drawable.ic_signal_refresh
                        )
                    )
                }.onFailure { e ->
                    Log.w(TAG, "Failed to start foreground service. Running without a foreground service.")
                }
            }
        }

       override fun close() {
            ApplicationDependencies.appForegroundObserver?.removeListener(this)
           closeNotificationController()
        }


        fun awaitResult(): Boolean {
            isRunning = true
            val success = runCatching {
                WebSocketDrainer.blockUntilDrainedAndProcessed()
            }.getOrDefault(false)
            isRunning = false
            return success
        }

        private fun closeNotificationController() {
            val controller = notificationController.get() ?: return

            runCatching {
                controller.close()
            }.getOrElse {e ->
                Log.w(TAG, "Exception thrown while closing notification controller", e)
            }
        }

        companion object {
            fun create(context: Context): ForegroundServiceController {
                val instance = ForegroundServiceController(context)
                ApplicationDependencies.appForegroundObserver?.addListener(instance)
                return instance
            }
        }
    }

    class Factory : Job.Factory<MessageFetchJob>{
        override fun create(parameters: Parameters, serializedData: ByteArray?): MessageFetchJob {
            return MessageFetchJob(parameters)
        }

    }

}