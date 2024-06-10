package org.thoughtcrime.securesms.database

import org.signal.core.util.logging.Log
import java.util.concurrent.TimeUnit

class RecipientTable {


    companion object {
        val TAG = Log.tag(RecipientTable::class.java)

        private val UNREGISTERED_LIFESPAN: Long = TimeUnit.DAYS.toMillis(30)

        const val TABLE_NAME = "recipient"

        const val ID = "_id"
        const val TYPE = "type"
        const val E164 = "e164"
        const val ACI_COLUMN = "aci"
        const val PNI_COLUMN = "pni"
        const val USERNAME = "username"
        const val EMAIL = "email"
        const val GROUP_ID = "group_id"
        const val DISTRIBUTION_LIST_ID = "distribution_list_id"
        const val CALL_LINK_ROOM_ID = "call_link_room_id"
        const val REGISTERED = "registered"
        const val UNREGISTERED_TIMESTAMP = "unregistered_timestamp"
        const val BLOCKED = "blocked"
        const val HIDDEN = "hidden"
        const val PROFILE_KEY = "profile_key"
        const val EXPIRING_PROFILE_KEY_CREDENTIAL = "profile_key_credential"
        const val PROFILE_SHARING = "profile_sharing"
        const val PROFILE_GIVEN_NAME = "profile_given_name"
        const val PROFILE_FAMILY_NAME = "profile_family_name"
        const val PROFILE_JOINED_NAME = "profile_joined_name"
        const val PROFILE_AVATAR = "profile_avatar"
        const val LAST_PROFILE_FETCH = "last_profile_fetch"
        const val SYSTEM_GIVEN_NAME = "system_given_name"
        const val SYSTEM_FAMILY_NAME = "system_family_name"
        const val SYSTEM_JOINED_NAME = "system_joined_name"
        const val SYSTEM_NICKNAME = "system_nickname"
        const val SYSTEM_PHOTO_URI = "system_photo_uri"
        const val SYSTEM_PHONE_LABEL = "system_phone_label"
        const val SYSTEM_PHONE_TYPE = "system_phone_type"
        const val SYSTEM_CONTACT_URI = "system_contact_uri"
        const val SYSTEM_INFO_PENDING = "system_info_pending"
        const val NOTIFICATION_CHANNEL = "notification_channel"
        const val MESSAGE_RINGTONE = "message_ringtone"
        const val MESSAGE_VIBRATE = "message_vibrate"
        const val CALL_RINGTONE = "call_ringtone"
        const val CALL_VIBRATE = "call_vibrate"
        const val MUTE_UNTIL = "mute_until"
        const val MESSAGE_EXPIRATION_TIME = "message_expiration_time"
        const val SEALED_SENDER_MODE = "sealed_sender_mode"
        const val STORAGE_SERVICE_ID = "storage_service_id"
        const val STORAGE_SERVICE_PROTO = "storage_service_proto"
        const val MENTION_SETTING = "mention_setting"
        const val CAPABILITIES = "capabilities"
        const val LAST_SESSION_RESET = "last_session_reset"
        const val WALLPAPER = "wallpaper"
        const val WALLPAPER_URI = "wallpaper_uri"
        const val ABOUT = "about"
        const val ABOUT_EMOJI = "about_emoji"
        const val EXTRAS = "extras"
        const val GROUPS_IN_COMMON = "groups_in_common"
        const val AVATAR_COLOR = "avatar_color"
        const val CHAT_COLORS = "chat_colors"
        const val CUSTOM_CHAT_COLORS_ID = "custom_chat_colors_id"
        const val BADGES = "badges"
        const val NEEDS_PNI_SIGNATURE = "needs_pni_signature"
        const val REPORTING_TOKEN = "reporting_token"
        const val PHONE_NUMBER_SHARING = "phone_number_sharing"
        const val PHONE_NUMBER_DISCOVERABLE = "phone_number_discoverable"
        const val PNI_SIGNATURE_VERIFIED = "pni_signature_verified"
        const val NICKNAME_GIVEN_NAME = "nickname_given_name"
        const val NICKNAME_FAMILY_NAME = "nickname_family_name"
        const val NICKNAME_JOINED_NAME = "nickname_joined_name"
        const val NOTE = "note"

        const val SEARCH_PROFILE_NAME = "search_signal_profile"
        const val SORT_NAME = "sort_name"
        const val IDENTITY_STATUS = "identity_status"
        const val IDENTITY_KEY = "identity_key"
    }
}