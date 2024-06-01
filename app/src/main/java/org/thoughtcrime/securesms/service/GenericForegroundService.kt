package org.thoughtcrime.securesms.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.DrawableRes
import org.thoughtcrime.securesms.jobs.UnableToStartException

class GenericForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    companion object {
        fun startForegroundTaskDelayed(
            context: Context,
            task: String,
            delayMillis: Long,
            @DrawableRes iconRes: Int
        ): DelayedNotificationController {

        }
        @Throws(UnableToStartException::class)
        fun startForegroundTask(
            context: Context,
            task: String,
            channelId: String = DEFAULT_ENTRY.channelId,
            @DrawableRes iconRes: Int = DEFAULT_ENTRY.iconRes
        ): NotificationController {

        }
    }
}