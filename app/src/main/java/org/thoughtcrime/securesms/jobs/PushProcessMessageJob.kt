package org.thoughtcrime.securesms.jobs

import org.signal.core.util.logging.Log

class PushProcessMessageJob(parameters: Parameters) : BaseJob(parameters) {
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
        const val KEY = "PushProcessMessageJobV2"
        const val QUEUE_PREFIX = "__PUSH_PROCESS_JOB__"

        private val TAG = Log.tag(PushProcessMessageJob::class.java)
    }
}