package org.thoughtcrime.securesms.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.DrawableRes
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.jobs.UnableToStartException
import org.thoughtcrime.securesms.notifications.NotificationChannels
import java.util.concurrent.atomic.AtomicInteger

class GenericForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    companion object {

        private val TAG = Log.tag(GenericForegroundService::class.java)

        private const val NOTIFICATION_ID = 827353982
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_CHANNEL_ID = "extra_channel_id"
        private const val EXTRA_ICON_RES = "extra_icon_res"
        private const val EXTRA_ID = "extra_id"
        private const val EXTRA_PROGRESS = "extra_progress"
        private const val EXTRA_PROGRESS_MAX = "extra_progress_max"
        private const val EXTRA_PROGRESS_INDETERMINATE = "extra_progress_indeterminate"
        private const val ACTION_START = "start"
        private const val ACTION_STOP = "stop"

        private val NEXT_ID = AtomicInteger()

        fun startForegroundTaskDelayed(
            context: Context,
            task: String,
            delayMillis: Long,
            @DrawableRes iconRes: Int
        ): DelayedNotificationController {
            return DelayedNotificationController()
        }

        @Throws(UnableToStartException::class)
        fun startForegroundTask(
            context: Context,
            task: String,
            channelId: String, //= DEFAULT_ENTRY.channelId,
            @DrawableRes iconRes: Int// = DEFAULT_ENTRY.iconRes
        ): NotificationController {
            return NotificationController()
        }
    }
}