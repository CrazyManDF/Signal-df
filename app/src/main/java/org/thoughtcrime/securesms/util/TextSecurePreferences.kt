package org.thoughtcrime.securesms.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.signal.core.util.logging.Log
import kotlin.concurrent.Volatile

object TextSecurePreferences {

    private val TAG = Log.tag(TextSecurePreferences::class.java)


    const val CHANGE_PASSPHRASE_PREF: String = "pref_change_passphrase"
    const val DISABLE_PASSPHRASE_PREF: String = "pref_disable_passphrase"
    const val THEME_PREF: String = "pref_theme"
    const val LANGUAGE_PREF: String = "pref_language"
    private const val MMSC_CUSTOM_HOST_PREF = "pref_apn_mmsc_custom_host"
    const val MMSC_HOST_PREF: String = "pref_apn_mmsc_host"
    private const val MMSC_CUSTOM_PROXY_PREF = "pref_apn_mms_custom_proxy"
    const val MMSC_PROXY_HOST_PREF: String = "pref_apn_mms_proxy"
    private const val MMSC_CUSTOM_PROXY_PORT_PREF = "pref_apn_mms_custom_proxy_port"
    const val MMSC_PROXY_PORT_PREF: String = "pref_apn_mms_proxy_port"
    private const val MMSC_CUSTOM_USERNAME_PREF = "pref_apn_mmsc_custom_username"
    const val MMSC_USERNAME_PREF: String = "pref_apn_mmsc_username"
    private const val MMSC_CUSTOM_PASSWORD_PREF = "pref_apn_mmsc_custom_password"
    const val MMSC_PASSWORD_PREF: String = "pref_apn_mmsc_password"
    const val ENABLE_MANUAL_MMS_PREF: String = "pref_enable_manual_mms"

    private const val LAST_VERSION_CODE_PREF = "last_version_code"
    const val RINGTONE_PREF: String = "pref_key_ringtone"
    const val VIBRATE_PREF: String = "pref_key_vibrate"
    private const val NOTIFICATION_PREF = "pref_key_enable_notifications"
    const val LED_COLOR_PREF: String = "pref_led_color"
    const val LED_BLINK_PREF: String = "pref_led_blink"
    private const val LED_BLINK_PREF_CUSTOM = "pref_led_blink_custom"
    const val ALL_MMS_PREF: String = "pref_all_mms"
    const val ALL_SMS_PREF: String = "pref_all_sms"
    const val PASSPHRASE_TIMEOUT_INTERVAL_PREF: String = "pref_timeout_interval"
    const val PASSPHRASE_TIMEOUT_PREF: String = "pref_timeout_passphrase"
    const val SCREEN_SECURITY_PREF: String = "pref_screen_security"
    private const val ENTER_SENDS_PREF = "pref_enter_sends"
    private const val ENTER_PRESENT_PREF = "pref_enter_key"
    private const val SMS_DELIVERY_REPORT_PREF = "pref_delivery_report_sms"
    const val MMS_USER_AGENT: String = "pref_mms_user_agent"
    private const val MMS_CUSTOM_USER_AGENT = "pref_custom_mms_user_agent"
    private const val SEEN_WELCOME_SCREEN_PREF = "pref_seen_welcome_screen"
    private const val PROMPTED_PUSH_REGISTRATION_PREF = "pref_prompted_push_registration"
    private const val PROMPTED_OPTIMIZE_DOZE_PREF = "pref_prompted_optimize_doze"
    private const val DIRECTORY_FRESH_TIME_PREF = "pref_directory_refresh_time"
    private const val UPDATE_APK_REFRESH_TIME_PREF = "pref_update_apk_refresh_time"
    private const val SIGNED_PREKEY_ROTATION_TIME_PREF = "pref_signed_pre_key_rotation_time"

    private const val IN_THREAD_NOTIFICATION_PREF = "pref_key_inthread_notifications"
    private const val SHOW_INVITE_REMINDER_PREF = "pref_show_invite_reminder"
    const val MESSAGE_BODY_TEXT_SIZE_PREF: String = "pref_message_body_text_size"

    private const val WIFI_SMS_PREF = "pref_wifi_sms"

    private const val RATING_LATER_PREF = "pref_rating_later"
    private const val RATING_ENABLED_PREF = "pref_rating_enabled"

    const val REPEAT_ALERTS_PREF: String = "pref_repeat_alerts"
    const val NOTIFICATION_PRIVACY_PREF: String = "pref_notification_privacy"
    const val NOTIFICATION_PRIORITY_PREF: String = "pref_notification_priority"
    const val NEW_CONTACTS_NOTIFICATIONS: String = "pref_enable_new_contacts_notifications"
    const val WEBRTC_CALLING_PREF: String = "pref_webrtc_calling"

    const val MEDIA_DOWNLOAD_MOBILE_PREF: String = "pref_media_download_mobile"
    const val MEDIA_DOWNLOAD_WIFI_PREF: String = "pref_media_download_wifi"
    const val MEDIA_DOWNLOAD_ROAMING_PREF: String = "pref_media_download_roaming"

    const val SYSTEM_EMOJI_PREF: String = "pref_system_emoji"
    private const val MULTI_DEVICE_PROVISIONED_PREF = "pref_multi_device"
    const val DIRECT_CAPTURE_CAMERA_ID: String = "pref_direct_capture_camera_id"
    const val ALWAYS_RELAY_CALLS_PREF: String = "pref_turn_only"
    const val READ_RECEIPTS_PREF: String = "pref_read_receipts"
    const val INCOGNITO_KEYBORAD_PREF: String = "pref_incognito_keyboard"
    private const val UNAUTHORIZED_RECEIVED = "pref_unauthorized_received"
    private const val SUCCESSFUL_DIRECTORY_PREF = "pref_successful_directory"

    private const val DATABASE_ENCRYPTED_SECRET = "pref_database_encrypted_secret"
    private const val DATABASE_UNENCRYPTED_SECRET = "pref_database_unencrypted_secret"
    private const val ATTACHMENT_ENCRYPTED_SECRET = "pref_attachment_encrypted_secret"
    private const val ATTACHMENT_UNENCRYPTED_SECRET = "pref_attachment_unencrypted_secret"
    private const val NEEDS_SQLCIPHER_MIGRATION = "pref_needs_sql_cipher_migration"

    const val CALL_NOTIFICATIONS_PREF: String = "pref_call_notifications"
    const val CALL_RINGTONE_PREF: String = "pref_call_ringtone"
    const val CALL_VIBRATE_PREF: String = "pref_call_vibrate"

    const val BACKUP: String = "pref_backup"
    const val BACKUP_ENABLED: String = "pref_backup_enabled"
    private const val BACKUP_PASSPHRASE = "pref_backup_passphrase"
    private const val ENCRYPTED_BACKUP_PASSPHRASE = "pref_encrypted_backup_passphrase"
    private const val BACKUP_TIME = "pref_backup_next_time"

    const val TRANSFER: String = "pref_transfer"

    const val SCREEN_LOCK: String = "pref_android_screen_lock"
    const val SCREEN_LOCK_TIMEOUT: String = "pref_android_screen_lock_timeout"

    @Deprecated("")
    val REGISTRATION_LOCK_PREF_V1: String = "pref_registration_lock"

    @Deprecated("")
    private val REGISTRATION_LOCK_PIN_PREF_V1 = "pref_registration_lock_pin"

    const val REGISTRATION_LOCK_PREF_V2: String = "pref_registration_lock_v2"

    private const val REGISTRATION_LOCK_LAST_REMINDER_TIME_POST_KBS =
        "pref_registration_lock_last_reminder_time_post_kbs"
    private const val REGISTRATION_LOCK_NEXT_REMINDER_INTERVAL =
        "pref_registration_lock_next_reminder_interval"

    const val SIGNAL_PIN_CHANGE: String = "pref_kbs_change"

    private const val SERVICE_OUTAGE = "pref_service_outage"
    private const val LAST_OUTAGE_CHECK_TIME = "pref_last_outage_check_time"

    private const val LAST_FULL_CONTACT_SYNC_TIME = "pref_last_full_contact_sync_time"
    private const val NEEDS_FULL_CONTACT_SYNC = "pref_needs_full_contact_sync"

    private const val LOG_ENCRYPTED_SECRET = "pref_log_encrypted_secret"
    private const val LOG_UNENCRYPTED_SECRET = "pref_log_unencrypted_secret"

    private const val NOTIFICATION_CHANNEL_VERSION = "pref_notification_channel_version"
    private const val NOTIFICATION_MESSAGES_CHANNEL_VERSION =
        "pref_notification_messages_channel_version"

    private const val NEEDS_MESSAGE_PULL = "pref_needs_message_pull"

    private const val UNIDENTIFIED_ACCESS_CERTIFICATE_ROTATION_TIME_PREF =
        "pref_unidentified_access_certificate_rotation_time"
    const val UNIVERSAL_UNIDENTIFIED_ACCESS: String = "pref_universal_unidentified_access"
    const val SHOW_UNIDENTIFIED_DELIVERY_INDICATORS: String =
        "pref_show_unidentifed_delivery_indicators"
    private const val UNIDENTIFIED_DELIVERY_ENABLED = "pref_unidentified_delivery_enabled"

    const val TYPING_INDICATORS: String = "pref_typing_indicators"

    const val LINK_PREVIEWS: String = "pref_link_previews"

    private const val SEEN_STICKER_INTRO_TOOLTIP = "pref_seen_sticker_intro_tooltip"

    private const val MEDIA_KEYBOARD_MODE = "pref_media_keyboard_mode"
    const val RECENT_STORAGE_KEY: String = "pref_recent_emoji2"

    private const val VIEW_ONCE_TOOLTIP_SEEN = "pref_revealable_message_tooltip_seen"

    private const val SEEN_CAMERA_FIRST_TOOLTIP = "pref_seen_camera_first_tooltip"

    private const val JOB_MANAGER_VERSION = "pref_job_manager_version"

    private const val APP_MIGRATION_VERSION = "pref_app_migration_version"

    private const val FIRST_INSTALL_VERSION = "pref_first_install_version"

    private const val HAS_SEEN_SWIPE_TO_REPLY = "pref_has_seen_swipe_to_reply"

    private const val HAS_SEEN_VIDEO_RECORDING_TOOLTIP =
        "camerax.fragment.has.dismissed.video.recording.tooltip"

    private const val STORAGE_MANIFEST_VERSION = "pref_storage_manifest_version"

    private const val ARGON2_TESTED = "argon2_tested"


    private val booleanPreferencesToBackup = arrayOf(
        SCREEN_SECURITY_PREF,
        INCOGNITO_KEYBORAD_PREF,
        ALWAYS_RELAY_CALLS_PREF,
        READ_RECEIPTS_PREF,
        TYPING_INDICATORS,
        SHOW_UNIDENTIFIED_DELIVERY_INDICATORS,
        UNIVERSAL_UNIDENTIFIED_ACCESS,
        NOTIFICATION_PREF,
        VIBRATE_PREF,
        IN_THREAD_NOTIFICATION_PREF,
        CALL_NOTIFICATIONS_PREF,
        CALL_VIBRATE_PREF,
        NEW_CONTACTS_NOTIFICATIONS,
        SHOW_INVITE_REMINDER_PREF,
        SYSTEM_EMOJI_PREF,
        ENTER_SENDS_PREF
    )

    private val stringPreferencesToBackup = arrayOf(
        LED_COLOR_PREF,
        LED_BLINK_PREF,
        REPEAT_ALERTS_PREF,
        NOTIFICATION_PRIVACY_PREF,
        THEME_PREF,
        LANGUAGE_PREF,
        MESSAGE_BODY_TEXT_SIZE_PREF
    )

    private val stringSetPreferencesToBackup = arrayOf(
        MEDIA_DOWNLOAD_MOBILE_PREF,
        MEDIA_DOWNLOAD_WIFI_PREF,
        MEDIA_DOWNLOAD_ROAMING_PREF
    )

    @Volatile
    private var preferences: SharedPreferences? = null

    private fun getSharedPreferences(context: Context): SharedPreferences {
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context)
        }
        return preferences!!
    }

    fun setBooleanPreference(context: Context, key: String, value: Boolean) {
        getSharedPreferences(context).edit().putBoolean(key, value).apply()
    }

    fun getBooleanPreference(context: Context, key: String, defaultValue: Boolean): Boolean {
        return getSharedPreferences(context).getBoolean(key, defaultValue)
    }

    fun setStringPreference(context: Context, key: String, value: String?) {
        getSharedPreferences(context).edit().putString(key, value).apply()
    }

    fun getStringPreference(context: Context, key: String, defaultValue: String?): String? {
        return getSharedPreferences(context).getString(key, defaultValue)
    }

    fun getIntegerPreference(context: Context, key: String, defaultValue: Int): Int {
        return getSharedPreferences(context).getInt(key, defaultValue)
    }

    private fun setIntegerPrefrence(context: Context, key: String, value: Int) {
        getSharedPreferences(context).edit().putInt(key, value).apply()
    }

    private fun setIntegerPrefrenceBlocking(context: Context, key: String, value: Int): Boolean {
        return getSharedPreferences(context).edit().putInt(key, value).commit()
    }

    fun getLongPreference(context: Context, key: String, defaultValue: Long): Long {
        return getSharedPreferences(context).getLong(key, defaultValue)
    }

    private fun setLongPreference(context: Context, key: String, value: Long) {
        getSharedPreferences(context).edit().putLong(key, value).apply()
    }

    private fun removePreference(context: Context, key: String) {
        getSharedPreferences(context).edit().remove(key).apply()
    }

    fun getPreferencesToSaveToBackupCount(context: Context): Long {
        val preferences = getSharedPreferences(context)
        var count: Long = 0

        for (booleanPreference in booleanPreferencesToBackup) {
            if (preferences.contains(booleanPreference)) {
                count++
            }
        }

        for (stringPreference in stringPreferencesToBackup) {
            if (preferences.contains(stringPreference)) {
                count++
            }
        }

        for (stringSetPreference in stringSetPreferencesToBackup) {
            if (preferences.contains(stringSetPreference)) {
                count++
            }
        }

        return count
    }

    fun getDatabaseUnencryptedSecret(context: Context): String? {
        return getStringPreference(context, DATABASE_UNENCRYPTED_SECRET, null)
    }

    fun getDatabaseEncryptedSecret(context: Context): String? {
        return getStringPreference(context, DATABASE_ENCRYPTED_SECRET, null)
    }

    fun setDatabaseEncryptedSecret(context: Context, secret: String) {
        setStringPreference(context, DATABASE_ENCRYPTED_SECRET, secret)
    }

    fun setDatabaseUnencryptedSecret(context: Context, secret: String?) {
        setStringPreference(context, DATABASE_UNENCRYPTED_SECRET, secret)
    }

    fun getAttachmentUnencryptedSecret(context: Context): String? {
        return getStringPreference(context, ATTACHMENT_UNENCRYPTED_SECRET, null)
    }

    fun getAttachmentEncryptedSecret(context: Context): String? {
        return getStringPreference(context, ATTACHMENT_ENCRYPTED_SECRET, null)
    }

    fun setAttachmentEncryptedSecret(context: Context, secret: String) {
        setStringPreference(context, ATTACHMENT_ENCRYPTED_SECRET, secret)
    }

    fun setAttachmentUnencryptedSecret(context: Context, secret: String?) {
        setStringPreference(context, ATTACHMENT_UNENCRYPTED_SECRET, secret)
    }

    fun setUnauthorizedReceived(context: Application, b: Boolean) {
        TODO("Not yet implemented")
    }
}