package org.thoughtcrime.securesms.database

import android.content.Context
import android.database.Cursor
import org.signal.core.util.SqlUtil
import org.signal.core.util.logging.Log
import org.signal.core.util.readToSingleObject
import org.signal.core.util.requireLong
import org.signal.core.util.select
import org.signal.core.util.toSingleLine
import org.thoughtcrime.securesms.recipients.RecipientId

class MessageTable(context: Context, databaseHelper: SignalDatabase) :
    DatabaseTable(context, databaseHelper) {

    companion object {
        private val TAG = Log.tag(MessageTable::class.java)
        const val TABLE_NAME = "message"
        const val ID = "_id"
        const val DATE_SENT = "date_sent"
        const val DATE_RECEIVED = "date_received"
        const val TYPE = "type"
        const val DATE_SERVER = "date_server"
        const val THREAD_ID = "thread_id"
        const val READ = "read"
        const val BODY = "body"
        const val FROM_RECIPIENT_ID = "from_recipient_id"
        const val FROM_DEVICE_ID = "from_device_id"
        const val TO_RECIPIENT_ID = "to_recipient_id"
        const val HAS_DELIVERY_RECEIPT = "has_delivery_receipt"
        const val HAS_READ_RECEIPT = "has_read_receipt"
        const val VIEWED_COLUMN = "viewed"
        const val MISMATCHED_IDENTITIES = "mismatched_identities"
        const val SMS_SUBSCRIPTION_ID = "subscription_id"
        const val EXPIRES_IN = "expires_in"
        const val EXPIRE_STARTED = "expire_started"
        const val NOTIFIED = "notified"
        const val NOTIFIED_TIMESTAMP = "notified_timestamp"
        const val UNIDENTIFIED = "unidentified"
        const val REACTIONS_UNREAD = "reactions_unread"
        const val REACTIONS_LAST_SEEN = "reactions_last_seen"
        const val REMOTE_DELETED = "remote_deleted"
        const val SERVER_GUID = "server_guid"
        const val RECEIPT_TIMESTAMP = "receipt_timestamp"
        const val EXPORT_STATE = "export_state"
        const val EXPORTED = "exported"
        const val MMS_CONTENT_LOCATION = "ct_l"
        const val MMS_EXPIRY = "exp"
        const val MMS_MESSAGE_TYPE = "m_type"
        const val MMS_MESSAGE_SIZE = "m_size"
        const val MMS_STATUS = "st"
        const val MMS_TRANSACTION_ID = "tr_id"
        const val NETWORK_FAILURES = "network_failures"
        const val QUOTE_ID = "quote_id"
        const val QUOTE_AUTHOR = "quote_author"
        const val QUOTE_BODY = "quote_body"
        const val QUOTE_MISSING = "quote_missing"
        const val QUOTE_BODY_RANGES = "quote_mentions"
        const val QUOTE_TYPE = "quote_type"
        const val SHARED_CONTACTS = "shared_contacts"
        const val LINK_PREVIEWS = "link_previews"
        const val MENTIONS_SELF = "mentions_self"
        const val MESSAGE_RANGES = "message_ranges"
        const val VIEW_ONCE = "view_once"
        const val STORY_TYPE = "story_type"
        const val PARENT_STORY_ID = "parent_story_id"
        const val SCHEDULED_DATE = "scheduled_date"
        const val LATEST_REVISION_ID = "latest_revision_id"
        const val ORIGINAL_MESSAGE_ID = "original_message_id"
        const val REVISION_NUMBER = "revision_number"
        const val MESSAGE_EXTRAS = "message_extras"

        const val QUOTE_NOT_PRESENT_ID = 0L
        const val QUOTE_TARGET_MISSING_ID = -1L

        const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT
                $DATE_SENT INTEGER NOT NULL
                $DATE_RECEIVED INTEGER NOT NULL,
                $DATE_SERVER INTEGER DEFAULT -1,
                $THREAD_ID INTEGER NOT NULL REFERENCES ${ThreadTable.TABLE_NAME} (${ThreadTable.ID}) ON DELETE CASCADE,
                $FROM_RECIPIENT_ID INTEGER NOT NULL REFERENCES ${RecipientTable.TABLE_NAME} (${RecipientTable.ID}) ON DELETE CASCADE,
                $FROM_DEVICE_ID INTEGER,
                $TO_RECIPIENT_ID INTEGER NOT NULL REFERENCES ${RecipientTable.TABLE_NAME} (${RecipientTable.ID}) ON DELETE CASCADE,
                $TYPE INTEGER NOT NULL,
                $BODY TEXT,
                $READ INTEGER DEFAULT 0,
                $MMS_CONTENT_LOCATION TEXT,
                $MMS_EXPIRY INTEGER,
                $MMS_MESSAGE_TYPE INTEGER,
                $MMS_MESSAGE_SIZE INTEGER,
                $MMS_STATUS INTEGER,
                $MMS_TRANSACTION_ID TEXT,
                $SMS_SUBSCRIPTION_ID INTEGER DEFAULT -1, 
                $RECEIPT_TIMESTAMP INTEGER DEFAULT -1, 
                $HAS_DELIVERY_RECEIPT INTEGER DEFAULT 0, 
                $HAS_READ_RECEIPT INTEGER DEFAULT 0, 
                $VIEWED_COLUMN INTEGER DEFAULT 0,
                $MISMATCHED_IDENTITIES TEXT DEFAULT NULL,
                $NETWORK_FAILURES TEXT DEFAULT NULL,
                $EXPIRES_IN INTEGER DEFAULT 0,
                $EXPIRE_STARTED INTEGER DEFAULT 0,
                $NOTIFIED INTEGER DEFAULT 0,
                $QUOTE_ID INTEGER DEFAULT 0,
                $QUOTE_AUTHOR INTEGER DEFAULT 0,
                $QUOTE_BODY TEXT DEFAULT NULL,
                $QUOTE_MISSING INTEGER DEFAULT 0,
                $QUOTE_BODY_RANGES BLOB DEFAULT NULL,
                $QUOTE_TYPE INTEGER DEFAULT 0,
                $SHARED_CONTACTS TEXT DEFAULT NULL,
                $UNIDENTIFIED INTEGER DEFAULT 0,
                $LINK_PREVIEWS TEXT DEFAULT NULL,
                $VIEW_ONCE INTEGER DEFAULT 0,
                $REACTIONS_UNREAD INTEGER DEFAULT 0,
                $REACTIONS_LAST_SEEN INTEGER DEFAULT -1,
                $REMOTE_DELETED INTEGER DEFAULT 0,
                $MENTIONS_SELF INTEGER DEFAULT 0,
                $NOTIFIED_TIMESTAMP INTEGER DEFAULT 0,
                $SERVER_GUID TEXT DEFAULT NULL,
                $MESSAGE_RANGES BLOB DEFAULT NULL,
                $STORY_TYPE INTEGER DEFAULT 0,
                $PARENT_STORY_ID INTEGER DEFAULT 0,
                $EXPORT_STATE BLOB DEFAULT NULL,
                $EXPORTED INTEGER DEFAULT 0,
                $SCHEDULED_DATE INTEGER DEFAULT -1,
                $LATEST_REVISION_ID INTEGER DEFAULT NULL REFERENCE $TABLE_NAME ($ID) ON DELETE CASCADE,
                $ORIGINAL_MESSAGE_ID INTEGER DEFAULT NULL REFERENCES $TABLE_NAME ($ID) ON DELETE CASCADE,
                $REVISION_NUMBER INTEGER DEFAULT 0,
                $MESSAGE_EXTRAS BLOB DEFAULT NULL
            )
        """

        private const val INDEX_THREAD_STORY_SCHEDULED_DATE_LATEST_REVISION_ID =
            "message_thread_story_parent_story_scheduled_date_latest_revision_id_index"
        private const val INDEX_DATE_SENT_FROM_TO_THREAD = "message_date_sent_from_to_thread_index"
        private const val INDEX_THREAD_COUNT = "message_thread_count_index"
        private const val INDEX_THREAD_UNREAD_COUNT = "message_thread_unread_count_index"

        val CREATE_INDEXS = arrayOf(
            "CREATE INDEX IF NOT EXISTS message_read_and_notified_and_thread_id_index ON $TABLE_NAME ($READ, $NOTIFIED, $THREAD_ID)",
            "CREATE INDEX IF NOT EXISTS message_type_index ON $TABLE_NAME ($TYPE)",
            "CREATE INDEX IF NOT EXISTS $INDEX_DATE_SENT_FROM_TO_THREAD ON $TABLE_NAME ($DATE_SENT, $FROM_RECIPIENT_ID, $TO_RECIPIENT_ID, $THREAD_ID)",
            "CREATE INDEX IF NOT EXISTS message_date_server_index ON $TABLE_NAME ($DATE_SERVER)",
            "CREATE INDEX IF NOT EXISTS message_reactions_unread_index ON $TABLE_NAME ($REACTIONS_UNREAD);",
            "CREATE INDEX IF NOT EXISTS message_story_type_index ON $TABLE_NAME ($STORY_TYPE);",
            "CREATE INDEX IF NOT EXISTS message_parent_story_id_index ON $TABLE_NAME ($PARENT_STORY_ID);",
            "CREATE INDEX IF NOT EXISTS $INDEX_THREAD_STORY_SCHEDULED_DATE_LATEST_REVISION_ID ON $TABLE_NAME ($THREAD_ID, $DATE_RECEIVED, $STORY_TYPE, $PARENT_STORY_ID, $SCHEDULED_DATE, $LATEST_REVISION_ID);",
            "CREATE INDEX IF NOT EXISTS message_quote_id_quote_author_scheduled_date_latest_revision_id_index ON $TABLE_NAME ($QUOTE_ID, $QUOTE_AUTHOR, $SCHEDULED_DATE, $LATEST_REVISION_ID);",
            "CREATE INDEX IF NOT EXISTS message_exported_index ON $TABLE_NAME ($EXPORTED);",
            "CREATE INDEX IF NOT EXISTS message_id_type_payment_transactions_index ON $TABLE_NAME ($ID,$TYPE) WHERE $TYPE & ${MessageTypes.SPECIAL_TYPE_PAYMENTS_NOTIFICATION} != 0;",
            "CREATE INDEX IF NOT EXISTS message_original_message_id_index ON $TABLE_NAME ($ORIGINAL_MESSAGE_ID);",
            "CREATE INDEX IF NOT EXISTS message_latest_revision_id_index ON $TABLE_NAME ($LATEST_REVISION_ID)",
            "CREATE INDEX IF NOT EXISTS message_from_recipient_id_index ON $TABLE_NAME ($FROM_RECIPIENT_ID)",
            "CREATE INDEX IF NOT EXISTS message_to_recipient_id_index ON $TABLE_NAME ($TO_RECIPIENT_ID)",
            "CREATE UNIQUE INDEX IF NOT EXISTS message_unique_sent_from_thread ON $TABLE_NAME ($DATE_SENT, $FROM_RECIPIENT_ID, $THREAD_ID)",
            // This index is created specifically for getting the number of messages in a thread and therefore needs to be kept in sync with that query
            "CREATE INDEX IF NOT EXISTS $INDEX_THREAD_COUNT ON $TABLE_NAME ($THREAD_ID) WHERE $STORY_TYPE = 0 AND $PARENT_STORY_ID <= 0 AND $SCHEDULED_DATE = -1 AND $LATEST_REVISION_ID IS NULL",
            // This index is created specifically for getting the number of unread messages in a thread and therefore needs to be kept in sync with that query
            "CREATE INDEX IF NOT EXISTS $INDEX_THREAD_UNREAD_COUNT ON $TABLE_NAME ($THREAD_ID) WHERE $STORY_TYPE = 0 AND $PARENT_STORY_ID <= 0 AND $SCHEDULED_DATE = -1 AND $LATEST_REVISION_ID IS NULL AND $READ = 0"
        )

        private val MMS_PROJECTION_BASE = arrayOf("")

        private val MMS_PROJECTION: Array<String> =
            MMS_PROJECTION_BASE + "NULL AS ${AttachmentTable.ATTACHMENT_JSON_ALIAS}"

        private val MMS_PROJECTION_WITH_ATTACHMENTS = arrayOf<String>()

        private const val IS_STORY_CLAUSE = "$STORY_TYPE > 0 AND $REMOTE_DELETED = 0"
        private const val RAW_ID_WHERE = "$TABLE_NAME.$ID = ?"

        private val SNIPPET_QUERY = """"""

        const val IS_CALL_TYPE_CLAUSE = """"""

        private val outgoingTypeClause: String by lazy {
            MessageTypes.OUTGOING_MESSAGE_TYPES
                .map { "($TABLE_NAME.$TYPE & ${MessageTypes.BASE_TYPE_MASK} = $it)" }
                .joinToString(" OR ")
        }


    }

    private val earlyDeliveryReceiptCache = EarlyDeliveryReceiptCache()

    private fun getOldestGroupUpdateSender(
        threadId: Long,
        minimumDateReceived: Long
    ): RecipientId? {
        val type = MessageTypes.SECURE_MESSAGE_BIT or MessageTypes.PUSH_MESSAGE_BIT or
                MessageTypes.GROUP_UPDATE_BIT or MessageTypes.BASE_INBOX_TYPE

        return getReadableDatabase()
            .select(FROM_DEVICE_ID)
            .from(TABLE_NAME)
            .where(
                "$THREAD_ID = ? AND $TYPE & ? AND $DATE_RECEIVED >= ?",
                threadId.toLong(), type.toString(), minimumDateReceived.toString()
            )
            .limit(1)
            .run()
            .readToSingleObject { RecipientId.from(it.requireLong(FROM_RECIPIENT_ID)) }
    }

    fun getExpirationStartedMessages(): Cursor {
        val where = "$EXPIRE_STARTED > 0"
        return rawQueryWithAttachments(where, null)
    }

    fun getMessageCursor(messageId: Long): Cursor {
        return internalGetMessage(messageId)
    }

    private fun internalGetMessage(messageId: Long): Cursor {
        return rawQueryWithAttachments(RAW_ID_WHERE, SqlUtil.buildArgs(messageId))
    }

    private fun rawQueryWithAttachments(
        where: String,
        arguments: Array<String>?,
        reverse: Boolean = false,
        limit: Long = 0
    ): Cursor {
        val database = databaseHelper.signalReadableDatabase
        var rawQueryString = """
            SELECT
                ${MMS_PROJECTION_WITH_ATTACHMENTS.joinToString(separator = ",")}
            FROM
                $TABLE_NAME LEFT OUTER JOIN ${AttachmentTable.TABLE_NAME} ON ($TABLE_NAME.$ID = ${AttachmentTable.TABLE_NAME}.${AttachmentTable.MESSAGE_ID})
            WHERE
                $where            
            GROUP BY 
                $TABLE_NAME.$ID
        """.toSingleLine()

        if (reverse) {
            rawQueryString += " ORDER BY $TABLE_NAME.$ID DESC"
        }

        if (limit > 0) {
            rawQueryString += " LIMIT $limit"
        }

        return database.rawQuery(rawQueryString, arguments)
    }

    fun incrementDeliveryReceiptCount(targetTimestamps: Long, receiptAuthor: RecipientId, receiptSentTimestamp: Long): Boolean {
        return true
    }
}