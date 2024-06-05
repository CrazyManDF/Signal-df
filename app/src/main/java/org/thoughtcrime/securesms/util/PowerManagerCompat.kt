package org.thoughtcrime.securesms.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object PowerManagerCompat {

    fun isDeviceIdleMode(powerManager: PowerManager): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            return powerManager.isDeviceIdleMode
        }
        return false
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return true
        }
        return ServiceUtil.getPowerManager(context)
            .isIgnoringBatteryOptimizations(context.packageName)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:" + context.packageName)
        )
        context.startActivity(intent)
    }
}