package org.thoughtcrime.securesms.messages

import android.content.Context
import com.squareup.wire.internal.toUnmodifiableList
import org.signal.core.util.logging.Log
import org.signal.core.util.roundedString
import org.signal.libsignal.metadata.InvalidMetadataMessageException
import org.signal.libsignal.metadata.InvalidMetadataVersionException
import org.signal.libsignal.metadata.ProtocolDuplicateMessageException
import org.signal.libsignal.metadata.ProtocolException
import org.signal.libsignal.metadata.ProtocolInvalidKeyException
import org.signal.libsignal.metadata.ProtocolInvalidKeyIdException
import org.signal.libsignal.metadata.ProtocolInvalidMessageException
import org.signal.libsignal.metadata.ProtocolInvalidVersionException
import org.signal.libsignal.metadata.ProtocolLegacyMessageException
import org.signal.libsignal.metadata.ProtocolNoSessionException
import org.signal.libsignal.metadata.ProtocolUntrustedIdentityException
import org.signal.libsignal.metadata.SelfSendException
import org.thoughtcrime.securesms.crypto.ReentrantSessionLock
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.groups.GroupId
import org.thoughtcrime.securesms.jobmanager.JobManager
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.messages.protocol.BufferedProtocolStore
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.FeatureFlags
import org.whispersystems.signalservice.api.crypto.EnvelopeMetadata
import org.whispersystems.signalservice.api.push.ServiceId
import org.whispersystems.signalservice.api.push.SignalServiceAddress
import org.whispersystems.signalservice.internal.push.Content
import org.whispersystems.signalservice.internal.push.Envelope
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

object MessageDecryptor {

    private val TAG = Log.tag(MessageDecryptor::class.java)

    fun decrypt(
        context: Context,
        bufferedProtocolStore: BufferedProtocolStore,
        envelope: Envelope,
        serverDeliveredTimestamp: Long
    ): Result {
        val selfAci: ServiceId.ACI = SignalStore.account().requireAci()
        val selfPni: ServiceId.PNI = SignalStore.account().requirePni()

        val destination: ServiceId? = ServiceId.parseOrNull(envelope.destinationServiceId)
        if (destination == null) {
            Log.w(TAG, "${logPrefix(envelope)} Missing destination address! Invalid message, ignoring.")
            return Result.Ignore(envelope, serverDeliveredTimestamp, emptyList())
        }

        if (destination != selfAci && destination != selfPni) {
            Log.w(TAG, "${logPrefix(envelope)} Destination address does not match our ACI or PNI! Invalid message, ignoring.")
            return Result.Ignore(envelope, serverDeliveredTimestamp, emptyList())
        }

        if (destination == selfPni && envelope.sourceServiceId != null) {
            Log.i(TAG, "${logPrefix(envelope)} Received a message at our PNI. Marking as needing a PNI signature.")

            val sourceServiceId = ServiceId.parseOrNull(envelope.sourceServiceId)

            if (sourceServiceId != null) {
                val sender = RecipientId.from(sourceServiceId)
                SignalDatabase.recipients.markNeedsPniSignature(sender)
            } else {
                Log.w(TAG, "${logPrefix(envelope)} Could not mark sender as needing a PNI signature because the sender serviceId was invalid!")
            }
        }

        if (destination == selfPni && envelope.sourceServiceId == null) {
            Log.w(TAG, "${logPrefix(envelope)} Got a sealed sender message to our PNI? Invalid message, ignoring.")
            return Result.Ignore(envelope, serverDeliveredTimestamp, emptyList())
        }

        val followUpOperations: MutableList<FollowUpOperation> = mutableListOf()

        if (envelope.type == Envelope.Type.PREKEY_BUNDLE) {
            Log.i(TAG, "${logPrefix(envelope)} Prekey message. Scheduling a prekey sync job.")
//            followUpOperations += FollowUpOperation {
//                PreKeysSyncJob.create().asChain()
//            }
        }

//        val bufferedStore = bufferedProtocolStore.get(destination)
//        val localAddress = SignalServiceAddress(selfAci, SignalStore.account().e164)
//        val cipher = SignalServiceCipher(localAddress, SignalStore.account().deviceId, bufferedStore, ReentrantSessionLock.INSTANCE, UnidentifiedAccessUtil.getCertificateValidator())

//        return try {
//            val startTimeNanos = System.nanoTime()
//            val cipherResult: SignalServiceCipherResult? = cipher.decrypt(envelope, serverDeliveredTimestamp)
//            val endTimeNanos = System.nanoTime()

//            if (cipherResult == null) {
//                Log.w(TAG, "${logPrefix(envelope)} Decryption resulted in a null result!", true)
//                return Result.Ignore(envelope, serverDeliveredTimestamp, followUpOperations.toUnmodifiableList())
//            }

//            Log.d(TAG, "${logPrefix(envelope, cipherResult)} Successfully decrypted the envelope in " +
//                    "${(endTimeNanos - startTimeNanos).nanoseconds.toDouble(DurationUnit.MILLISECONDS)
//                        .roundedString(2)} ms  (GUID ${envelope.serverGuid})." +
//                    " Delivery latency: ${serverDeliveredTimestamp - envelope.serverTimestamp!!} ms, Urgent: ${envelope.urgent}")
//
//            val validationResult: EnvelopeContentValidator.Result = EnvelopeContentValidator.validate(envelope, cipherResult.content)

//            if (validationResult is EnvelopeContentValidator.Result.Invalid) {
//                Log.w(TAG, "${logPrefix(envelope, cipherResult)} Invalid content! ${validationResult.reason}", validationResult.throwable)
//
//                if (FeatureFlags.internalUser()) {
//                    postInvalidMessageNotification(context, validationResult.reason)
//                }
//
//                return Result.Ignore(envelope, serverDeliveredTimestamp, followUpOperations.toUnmodifiableList())
//            }
//
//            if (validationResult is EnvelopeContentValidator.Result.UnsupportedDataMessage) {
//                Log.w(TAG, "${logPrefix(envelope, cipherResult)} Unsupported DataMessage! Our version: ${validationResult.ourVersion}, their version: ${validationResult.theirVersion}")
//                return Result.UnsupportedDataMessage(envelope, serverDeliveredTimestamp, cipherResult.toErrorMetadata(), followUpOperations.toUnmodifiableList())
//            }
//
//            // Must handle SKDM's immediately, because subsequent decryptions could rely on it
//            if (cipherResult.content.senderKeyDistributionMessage != null) {
//                handleSenderKeyDistributionMessage(
//                    envelope,
//                    cipherResult.metadata.sourceServiceId,
//                    cipherResult.metadata.sourceDeviceId,
//                    SenderKeyDistributionMessage(cipherResult.content.senderKeyDistributionMessage!!.toByteArray()),
//                    bufferedProtocolStore.getAciStore()
//                )
//            }
//
//            if (cipherResult.content.pniSignatureMessage != null) {
//                if (cipherResult.metadata.sourceServiceId is ServiceId.ACI) {
//                    handlePniSignatureMessage(
//                        envelope,
//                        bufferedProtocolStore,
//                        cipherResult.metadata.sourceServiceId as ServiceId.ACI,
//                        cipherResult.metadata.sourceE164,
//                        cipherResult.metadata.sourceDeviceId,
//                        cipherResult.content.pniSignatureMessage!!
//                    )
//                } else {
//                    Log.w(TAG, "${logPrefix(envelope)} Ignoring PNI signature because the sourceServiceId isn't an ACI!")
//                }
//            } else if (cipherResult.content.pniSignatureMessage != null) {
//                Log.w(TAG, "${logPrefix(envelope)} Ignoring PNI signature because the feature flag is disabled!")
//            }

            // TODO We can move this to the "message processing" stage once we give it access to the envelope. But for now it'll stay here.
//            if (envelope.reportingToken != null && envelope.reportingToken!!.size > 0) {
//                val sender = RecipientId.from(cipherResult.metadata.sourceServiceId)
//                SignalDatabase.recipients.setReportingToken(sender, envelope.reportingToken!!.toByteArray())
//            }

//            Result.Success(envelope, serverDeliveredTimestamp, cipherResult.content, cipherResult.metadata, followUpOperations.toUnmodifiableList())
//        }catch (e: Exception){
//            when (e) {
//                is ProtocolInvalidKeyIdException,
//                is ProtocolInvalidKeyException,
//                is ProtocolUntrustedIdentityException,
//                is ProtocolNoSessionException,
//                is ProtocolInvalidMessageException -> {
//                    check(e is ProtocolException)
//                    Log.w(TAG, "${logPrefix(envelope, e)} Decryption error!", e, true)
//
//                    if (FeatureFlags.internalUser()) {
//                        postDecryptionErrorNotification(context)
//                    }
//
//                    if (FeatureFlags.retryReceipts()) {
//                        buildResultForDecryptionError(context, envelope, serverDeliveredTimestamp, followUpOperations, e)
//                    } else {
//                        Log.w(TAG, "${logPrefix(envelope, e)} Retry receipts disabled! Enqueuing a session reset job, which will also insert an error message.", e, true)
//
//                        followUpOperations += FollowUpOperation {
//                            val sender: Recipient = Recipient.external(context, e.sender)
//                            AutomaticSessionResetJob(sender.id, e.senderDevice, envelope.timestamp!!).asChain()
//                        }
//
//                        Result.Ignore(envelope, serverDeliveredTimestamp, followUpOperations.toUnmodifiableList())
//                    }
//                }
//
//                is ProtocolDuplicateMessageException -> {
//                    Log.w(TAG, "${logPrefix(envelope, e)} Duplicate message!", e)
//                    Result.Ignore(envelope, serverDeliveredTimestamp, followUpOperations.toUnmodifiableList())
//                }
//
//                is InvalidMetadataVersionException,
//                is InvalidMetadataMessageException,
//                is InvalidMessageStructureException -> {
//                    Log.w(TAG, "${logPrefix(envelope)} Invalid message structure!", e, true)
//                    Result.Ignore(envelope, serverDeliveredTimestamp, followUpOperations.toUnmodifiableList())
//                }
//
//                is SelfSendException -> {
//                    Log.i(TAG, "[${envelope.timestamp}] Dropping sealed sender message from self!", e)
//                    Result.Ignore(envelope, serverDeliveredTimestamp, followUpOperations.toUnmodifiableList())
//                }
//
//                is ProtocolInvalidVersionException -> {
//                    Log.w(TAG, "${logPrefix(envelope, e)} Invalid version!", e, true)
//                    Result.InvalidVersion(envelope, serverDeliveredTimestamp, e.toErrorMetadata(), followUpOperations.toUnmodifiableList())
//                }
//
//                is ProtocolLegacyMessageException -> {
//                    Log.w(TAG, "${logPrefix(envelope, e)} Legacy message!", e, true)
//                    Result.LegacyMessage(envelope, serverDeliveredTimestamp, e.toErrorMetadata(), followUpOperations)
//                }
//
//                else -> {
//                    Log.w(TAG, "Encountered an unexpected exception! Throwing!", e, true)
//                    throw e
//                }
//            }
//        }
        return Result.Ignore(envelope, serverDeliveredTimestamp, followUpOperations.toUnmodifiableList())
    }

    private fun logPrefix(envelope: Envelope): String {
        return logPrefix(envelope.timestamp!!, ServiceId.parseOrNull(envelope.sourceServiceId)?.logString() ?: "<sealed>", envelope.sourceDevice)
    }

//    private fun logPrefix(envelope: Envelope, cipherResult: SignalServiceCipherResult): String {
//        return logPrefix(envelope.timestamp!!, cipherResult.metadata.sourceServiceId.logString(), cipherResult.metadata.sourceDeviceId)
//    }

    private fun logPrefix(timestamp: Long, sender: String?, deviceId: Int?): String {
        val senderString = sender ?: "null"
        return "[$timestamp] $senderString:${deviceId ?: 0} |"
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
        val groupId: GroupId?
    )

    data class DecryptionErrorCount(
        var count: Int,
        var lastReceivedTime: Long
    )

    fun interface FollowUpOperation {
        fun run(): JobManager.Chain?
    }
}