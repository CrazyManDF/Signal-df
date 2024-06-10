package org.thoughtcrime.securesms.database

import org.signal.core.util.logging.Log

class AttachmentTable {

    companion object {
        val TAG = Log.tag(AttachmentTable::class.java)

        const val TABLE_NAME = "attachment"
        const val ID = "_id"
        const val MESSAGE_ID = "message_id"
        const val CONTENT_TYPE = "content_type"
        const val REMOTE_KEY = "remote_key"
        const val REMOTE_LOCATION = "remote_location"
        const val REMOTE_DIGEST = "remote_digest"
        const val REMOTE_INCREMENTAL_DIGEST = "remote_incremental_digest"
        const val REMOTE_INCREMENTAL_DIGEST_CHUNK_SIZE = "remote_incremental_digest_chunk_size"
        const val CDN_NUMBER = "cdn_number"
        const val TRANSFER_STATE = "transfer_state"
        const val TRANSFER_FILE = "transfer_file"
        const val DATA_FILE = "data_file"
        const val DATA_SIZE = "data_size"
        const val DATA_RANDOM = "data_random"
        const val DATA_HASH_START = "data_hash_start"
        const val DATA_HASH_END = "data_hash_end"
        const val FILE_NAME = "file_name"
        const val FAST_PREFLIGHT_ID = "fast_preflight_id"
        const val VOICE_NOTE = "voice_note"
        const val BORDERLESS = "borderless"
        const val VIDEO_GIF = "video_gif"
        const val QUOTE = "quote"
        const val WIDTH = "width"
        const val HEIGHT = "height"
        const val CAPTION = "caption"
        const val STICKER_PACK_ID = "sticker_pack_id"
        const val STICKER_PACK_KEY = "sticker_pack_key"
        const val STICKER_ID = "sticker_id"
        const val STICKER_EMOJI = "sticker_emoji"
        const val BLUR_HASH = "blur_hash"
        const val TRANSFORM_PROPERTIES = "transform_properties"
        const val DISPLAY_ORDER = "display_order"
        const val UPLOAD_TIMESTAMP = "upload_timestamp"
        const val ARCHIVE_CDN = "archive_cdn"
        const val ARCHIVE_MEDIA_NAME = "archive_media_name"
        const val ARCHIVE_MEDIA_ID = "archive_media_id"
        const val ARCHIVE_TRANSFER_FILE = "archive_transfer_file"
        const val ARCHIVE_TRANSFER_STATE = "archive_transfer_state"

        const val ATTACHMENT_JSON_ALIAS = "attachment_json"

        private const val DIRECTORY = "parts"

        const val TRANSFER_PROGRESS_DONE = 0
        const val TRANSFER_PROGRESS_STARTED = 1
        const val TRANSFER_PROGRESS_PENDING = 2
        const val TRANSFER_PROGRESS_FAILED = 3
        const val TRANSFER_PROGRESS_PERMANENT_FAILURE = 4
        const val TRANSFER_NEEDS_RESTORE = 5
        const val TRANSFER_RESTORE_IN_PROGRESS = 6
        const val PREUPLOAD_MESSAGE_ID: Long = -8675309
    }
}