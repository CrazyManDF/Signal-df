package org.thoughtcrime.securesms.messages

import android.content.Context
import org.thoughtcrime.securesms.jobmanager.JobManager
import org.thoughtcrime.securesms.messages.protocol.BufferedProtocolStore
import org.whispersystems.signalservice.api.crypto.EnvelopeMetadata
import org.whispersystems.signalservice.internal.push.Content
import org.whispersystems.signalservice.internal.push.Envelope

object MessageDecryptor {

    fun decrypt(
        context: Context,
        bufferedProtocolStore: BufferedProtocolStore,
        envelope: Envelope,
        serverDeliveredTimestamp: Long
    ): Result {

    }

    sealed interface Result {
        val envelope: Envelope
        val serverDeliveredTimestamp: Long
        val followUpOperations: List<FollowUpOperation>

        /** Successfully decrypted the envelope content. The plaintext [Content] is available. */
        data class Success(
            override val envelope: Envelope,
            override val serverDeliveredTimestamp: Long,
            val content: Content,
            val metadata: EnvelopeMetadata,
            override val followUpOperations: List<FollowUpOperation>
        ) : Result

        /** We could not decrypt the message, and an error should be inserted into the user's chat history. */
        class DecryptionError(
            override val envelope: Envelope,
            override val serverDeliveredTimestamp: Long,
            override val errorMetadata: ErrorMetadata,
            override val followUpOperations: List<FollowUpOperation>
        ) : Result, Error

        /** The envelope used an invalid version of the Signal protocol. */
        class InvalidVersion(
            override val envelope: Envelope,
            override val serverDeliveredTimestamp: Long,
            override val errorMetadata: ErrorMetadata,
            override val followUpOperations: List<FollowUpOperation>
        ) : Result, Error

        /** The envelope used an old format that hasn't been used since 2015. This shouldn't be happening. */
        class LegacyMessage(
            override val envelope: Envelope,
            override val serverDeliveredTimestamp: Long,
            override val errorMetadata: ErrorMetadata,
            override val followUpOperations: List<FollowUpOperation>
        ) : Result, Error

        /**
         * Indicates the that the [org.whispersystems.signalservice.internal.push.SignalServiceProtos.DataMessage.getRequiredProtocolVersion]
         * is higher than we support.
         */
        class UnsupportedDataMessage(
            override val envelope: Envelope,
            override val serverDeliveredTimestamp: Long,
            override val errorMetadata: ErrorMetadata,
            override val followUpOperations: List<FollowUpOperation>
        ) : Result, Error

        /** There are no further results from this envelope that need to be processed. There may still be [followUpOperations]. */
        class Ignore(
            override val envelope: Envelope,
            override val serverDeliveredTimestamp: Long,
            override val followUpOperations: List<FollowUpOperation>
        ) : Result

        interface Error {
            val errorMetadata: ErrorMetadata
        }
    }

    data class ErrorMetadata(
        val sender: String,
        val senderDevice: Int,
//        val groupId: GroupId?
    )

    data class DecryptionErrorCount(
        var count: Int,
        var lastReceivedTime: Long
    )

    fun interface FollowUpOperation {
        fun run(): JobManager.Chain?
    }
}