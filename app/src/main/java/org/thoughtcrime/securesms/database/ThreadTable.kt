package org.thoughtcrime.securesms.database

import org.signal.core.util.logging.Log

class ThreadTable {

    companion object {
        private val TAG = Log.tag(ThreadTable::class.java)

        const val TABLE_NAME = "thread"
        const val ID = "_id"
        const val DATE = "date"
        const val MEANINGFUL_MESSAGES = "meaningful_messages"
        const val RECIPIENT_ID = "recipient_id"
        const val READ = "read"
        const val UNREAD_COUNT = "unread_count"
        const val TYPE = "type"
        const val ERROR = "error"
        const val SNIPPET = "snippet"
        const val SNIPPET_TYPE = "snippet_type"
        const val SNIPPET_URI = "snippet_uri"
        const val SNIPPET_CONTENT_TYPE = "snippet_content_type"
        const val SNIPPET_EXTRAS = "snippet_extras"
        const val SNIPPET_MESSAGE_EXTRAS = "snippet_message_extras"
        const val ARCHIVED = "archived"
        const val STATUS = "status"
        const val HAS_DELIVERY_RECEIPT = "has_delivery_receipt"
        const val HAS_READ_RECEIPT = "has_read_receipt"
        const val EXPIRES_IN = "expires_in"
        const val LAST_SEEN = "last_seen"
        const val HAS_SENT = "has_sent"
        const val LAST_SCROLLED = "last_scrolled"
        const val PINNED = "pinned"
        const val UNREAD_SELF_MENTION_COUNT = "unread_self_mention_count"
        const val ACTIVE = "active"

        const val MAX_CACHE_SIZE = 1000
    }
}