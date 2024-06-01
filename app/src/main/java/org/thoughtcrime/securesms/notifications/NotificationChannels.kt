package org.thoughtcrime.securesms.notifications

import android.app.Application
import org.signal.core.util.logging.Log.tag
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies

class NotificationChannels private constructor(application: Application){

    private val EMPTY_VIBRATION_PATTERN = longArrayOf(0)

    class Version {
        val MESSAGES_CATEGORY: Int = 2
        val CALLS_PRIORITY_BUMP: Int = 3
        val VIBRATE_OFF_OTHER: Int = 4
        val AUDIO_ATTRIBUTE_CHANGE: Int = 5
    }

    private val VERSION = 5

    private val CATEGORY_MESSAGES = "messages"
    private val CONTACT_PREFIX = "contact_"
    private val MESSAGES_PREFIX = "messages_"

    val CALLS: String = "calls_v3"
    val FAILURES: String = "failures"
    val APP_UPDATES: String = "app_updates"
    val BACKUPS: String = "backups_v2"
    val LOCKED_STATUS: String = "locked_status_v2"
    val OTHER: String = "other_v3"
    val VOICE_NOTES: String = "voice_notes"
    val JOIN_EVENTS: String = "join_events"
    val BACKGROUND: String = "background_connection"
    val CALL_STATUS: String = "call_status"
    val APP_ALERTS: String = "app_alerts"
    val ADDITIONAL_MESSAGE_NOTIFICATIONS: String = "additional_message_notifications"

    companion object {
        private val TAG = tag(NotificationChannels::class.java)

        val instance: NotificationChannels by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NotificationChannels(ApplicationDependencies.getApplication())
        }
    }
}