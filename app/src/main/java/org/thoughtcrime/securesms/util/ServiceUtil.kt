package org.thoughtcrime.securesms.util

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.PowerManager

object ServiceUtil {

    fun getPowerManager(context: Context): PowerManager {
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    fun getConnectivityManager(context: Context): ConnectivityManager {
        return context.getSystemService(Activity.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun getAlarmManager(context: Context): AlarmManager {
        return context.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
    }
}