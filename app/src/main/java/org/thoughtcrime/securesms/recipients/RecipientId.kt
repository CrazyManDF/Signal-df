package org.thoughtcrime.securesms.recipients

import android.annotation.SuppressLint
import android.os.Parcel
import androidx.annotation.AnyThread
import org.signal.core.util.LongSerializer
import org.thoughtcrime.securesms.database.SignalDatabase
import org.whispersystems.signalservice.api.push.ServiceId

class RecipientId(private val id: Long) {

    constructor(inParcel: Parcel) : this(inParcel.readLong())

    companion object {

        private const val TAG = "RecipientId"
        private const val UNKNOWN_ID: Long = -1
        private const val DELIMITER = ','

        val UNKNOWN: RecipientId = from(UNKNOWN_ID)

        @AnyThread
        fun from(serviceId: ServiceId): RecipientId {
            return from(serviceId, null)
        }

        @AnyThread
        @SuppressLint("WrongThread")
        private fun from(serviceId: ServiceId?, e164: String?): RecipientId {
            if (serviceId != null && serviceId.isUnknown) {
                return UNKNOWN
            }

//            var recipientId: RecipientId = RecipientIdCache.INSTANCE.get(serviceId, e164)

//            if (recipientId == null) {
//                recipientId = SignalDatabase.recipients.getAndPossiblyMerge(serviceId, e164)
//                RecipientIdCache.INSTANCE.put(recipientId, e164, serviceId)
//            }

//            return recipientId
            return RecipientId(0)
        }

        fun from(id: Long): RecipientId {
            if (id == 0L) {
                throw InvalidLongRecipientIdError()
            }

            return RecipientId(id)
        }
    }

    fun toLong(): Long {
        return id
    }

    private class InvalidLongRecipientIdError : AssertionError()
    private class InvalidStringRecipientIdError : AssertionError()

    private class Serializer : LongSerializer<RecipientId>{
        override fun serialize(data: RecipientId): Long {
            return data.toLong()
        }

        override fun deserialize(data: Long): RecipientId {
            return from(data)
        }
    }
}