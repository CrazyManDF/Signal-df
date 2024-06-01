package org.thoughtcrime.securesms.jobs

import org.signal.core.util.logging.Log

class TypingSendJob(parameters: Parameters) : BaseJob(parameters) {
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
        const val KEY: String = "TypingSendJob"

        private val TAG = Log.tag(TypingSendJob::class.java)
    }
}