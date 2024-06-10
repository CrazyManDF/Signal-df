package org.thoughtcrime.securesms.recipients

import android.os.Parcel
import org.signal.core.util.LongSerializer

class RecipientId(private val id: Long) {

    constructor(inParcel: Parcel) : this(inParcel.readLong())

    companion object {

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