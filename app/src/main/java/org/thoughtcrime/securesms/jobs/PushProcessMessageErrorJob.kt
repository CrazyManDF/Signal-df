package org.thoughtcrime.securesms.jobs

import android.content.Context
import androidx.annotation.WorkerThread
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.messages.ExceptionMetadata
import org.thoughtcrime.securesms.messages.MessageState

class PushProcessMessageErrorJob private constructor(
    parameters: Parameters,
    private val messageState: MessageState,
    private val exceptionMetadata: ExceptionMetadata,
    private val timestamp: Long
) : BaseJob(parameters){

    constructor(messageState: MessageState, exceptionMetadata: ExceptionMetadata, timestamp: Long) : this(
        parameters = createParameters(exceptionMetadata),
        messageState = messageState,
        exceptionMetadata = exceptionMetadata,
        timestamp = timestamp
    )

    override fun onRun() {
        TODO("Not yet implemented")
    }

    override fun onShouldRetry(e: Exception): Boolean {
        TODO("Not yet implemented")
    }

    override fun serialize(): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun getFactoryKey(): String {
        TODO("Not yet implemented")
    }

    override fun onFailure() {
        TODO("Not yet implemented")
    }


    companion object {
        const val KEY = "PushProcessMessageErrorV2Job"

        val TAG = Log.tag(PushProcessMessageErrorJob::class.java)

        private const val KEY_MESSAGE_STATE = "message_state"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_EXCEPTION_SENDER = "exception_sender"
        private const val KEY_EXCEPTION_DEVICE = "exception_device"
        private const val KEY_EXCEPTION_GROUP_ID = "exception_groupId"

        @WorkerThread
        private fun createParameters(exceptionMetadata: ExceptionMetadata): Parameters {
            val context: Context = ApplicationDependencies.getApplication()

//            val recipient = exceptionMetadata.groupId?.let { Recipient.externalPossiblyMigratedGroup(it) } ?: Recipient.external(context, exceptionMetadata.sender)

            return Parameters.Builder()
//                .setMaxAttempts(Parameters.UNLIMITED)
//                .addConstraint(ChangeNumberConstraint.KEY)
//                .setQueue(PushProcessMessageJob.getQueueName(recipient.id))
                .build()
        }
    }
}