package org.thoughtcrime.securesms.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import org.signal.core.util.PendingIntentFlags
import org.signal.core.util.logging.Log
import org.whispersystems.signalservice.api.util.SleepTimer
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AlarmSleepTimer(private val context: Context) : SleepTimer {

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val actionIdList: ConcurrentSkipListSet<Int> = ConcurrentSkipListSet()

    override fun sleep(sleepDuration: Long) {
        val alarmReceiver = AlarmReceiver()
        var actionId = 0

        while (!actionIdList.add(actionId)) {
            actionId++
        }
        try {
            val actionName = buildActionName(actionId)
            context.registerReceiver(alarmReceiver, IntentFilter(actionName))

            val startTime = System.currentTimeMillis()
            alarmReceiver.setAlarm(sleepDuration, actionName)

            while (System.currentTimeMillis() - startTime < sleepDuration){
                try {
                    lock.withLock{
                        condition.await(sleepDuration - (System.currentTimeMillis()-startTime),TimeUnit.MILLISECONDS)
                    }
                }catch (e: InterruptedException){
                    e.printStackTrace()
                    Log.w(TAG, e)
                }
            }
            context.unregisterReceiver(alarmReceiver)
        }catch (e: Exception){
            Log.w(TAG, "Exception during sleep ...", e)
        }finally {
            actionIdList.remove(actionId)
        }
    }

    private fun buildActionName(actionId: Int): String {
        return "$WAKE_UP_THREAD_ACTION.$actionId"
    }

    private inner class AlarmReceiver : BroadcastReceiver() {

        fun setAlarm(millis: Long, actionName: String) {
            val intent = Intent(actionName)
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntentFlags.mutable())
            val alarmManager = ServiceUtil.getAlarmManager(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Setting an exact alarm to wake up in " + millis + "ms.")
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + millis,
                    pendingIntent
                )
            } else {
                Log.w(
                    TAG,
                    "Setting an inexact alarm to wake up in " + millis + "ms. CanScheduleAlarms: " + alarmManager.canScheduleExactAlarms()
                )
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + millis,
                    pendingIntent
                )
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    companion object {
        private val TAG = Log.tag(AlarmSleepTimer::class.java)
        val WAKE_UP_THREAD_ACTION =
            "org.thoughtcrime.securesms.util.AlarmSleepTimer.AlarmReceiver.WAKE_UP_THREAD"
    }
}