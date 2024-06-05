package org.signal.core.util

import android.app.PendingIntent
import android.os.Build

object PendingIntentFlags {

    fun updateCurrent(): Int {
        return mutable() or PendingIntent.FLAG_UPDATE_CURRENT
    }

    fun cancelCurrent(): Int {
        return mutable() or PendingIntent.FLAG_CANCEL_CURRENT
    }

    fun oneShot(): Int {
        return mutable() or PendingIntent.FLAG_ONE_SHOT
    }

    fun mutable(): Int {
        return if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
    }

    fun immutable(): Int {
        return if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
    }
}