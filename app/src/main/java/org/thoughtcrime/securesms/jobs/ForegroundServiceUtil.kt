package org.thoughtcrime.securesms.jobs

import android.app.AlarmManager
import android.app.ForegroundServiceStartNotAllowedException
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import org.signal.core.util.PendingIntentFlags
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.util.ServiceUtil
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.minutes

object ForegroundServiceUtil {

    private val TAG = Log.tag(ForegroundServiceUtil::class.java)

    private val updateMutex: ReentrantLock = ReentrantLock()
    private var activeLatch: CountDownLatch? = null

    private val DEFAULT_TIMEOUT: Long = 1.minutes.inWholeMilliseconds

    @Throws(UnableToStartException::class)
    @WorkerThread
    fun startWhenCapable(context: Context, intent: Intent, timeout: Long = DEFAULT_TIMEOUT) {
        try {
            start(context, intent)
        } catch (e: UnableToStartException) {
            Log.w(TAG, "Failed to start normally. Blocking and then trying again.")
            blockUntilCapable(context, timeout)
            start(context, intent)
        }
    }

    @Throws(UnableToStartException::class)
    fun start(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT < 31) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: IllegalStateException) {
                if (e is ForegroundServiceStartNotAllowedException) {
                    Log.e(TAG, "Unable to start foreground service", e)
                    throw UnableToStartException(e)
                } else {
                    throw e
                }
            }
        }
    }

    private fun blockUntilCapable(context: Context, timeout: Long = DEFAULT_TIMEOUT): Boolean {
        val alarmManager = ServiceUtil.getAlarmManager(context)

        if (Build.VERSION.SDK_INT < 31 || ApplicationDependencies.appForegroundObserver!!.isForegrounded()) {
            return true
        }

        if (!alarmManager.canScheduleExactAlarms()) {
            return false
        }

        val latch: CountDownLatch? = updateMutex.withLock {
            if (activeLatch == null) {
                if (alarmManager.canScheduleExactAlarms()) {
                    activeLatch = CountDownLatch(1)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(context, Receiver::class.java),
                        PendingIntentFlags.mutable()
                    )
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 1000,
                        pendingIntent
                    )
                }
            }
            activeLatch
        }

        if (latch != null) {
            try {
                if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                    Log.w(TAG, "Time ran out waiting for foreground")
                    return false
                }
            } catch (e: InterruptedException) {
                Log.w(TAG, "Interrupted while waiting for foreground")
            }
        }

        return true
    }

    class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateMutex.withLock {
                activeLatch?.countDown()
                activeLatch = null
            }
        }
    }
}

class UnableToStartException(cause: Throwable) : Exception(cause)