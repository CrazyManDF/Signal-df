package org.thoughtcrime.securesms.net

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.signal.core.util.PendingIntentFlags
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.keyvalue.InternalValues
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.notifications.NotificationChannels
import org.thoughtcrime.securesms.util.FeatureFlags
import org.whispersystems.signalservice.internal.websocket.WebSocketShadowingBridge

class DefaultWebSocketShadowingBridge(private val context: Application) : WebSocketShadowingBridge {

    private val store: InternalValues = SignalStore.internalValues()
    override fun writeStatsSnapshot(bytes: ByteArray) {
        store.setWebSocketShadowingStats(bytes)
    }

    override fun readStatsSnapshot(): ByteArray? {
        return store.getWebSocketShadowingStats(null)
    }

    override fun triggerFailureNotification(message: String) {
//        if (!FeatureFlags.internalUser()) {
//            return
//        }
//        val notification: Notification = NotificationCompat.Builder(context, NotificationChannels.instance.FAILURES)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle("[Internal-only] $message")
//            .setContentText("Tap to send a debug log")
//            .setContentIntent(
//                PendingIntent.getActivity(
//                    context,
//                    0,
//                    Intent(context, SubmitDebugLogActivity::class.java),
//                    PendingIntentFlags.mutable()
//                )
//            )
//            .build()
//
//        NotificationManagerCompat.from(context).notify(NotificationIds.INTERNAL_ERROR, notification)
    }
}