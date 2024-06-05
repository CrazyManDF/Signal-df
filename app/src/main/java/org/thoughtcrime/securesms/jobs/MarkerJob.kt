package org.thoughtcrime.securesms.jobs

import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.jobmanager.Job

class MarkerJob(parameters: Parameters) : BaseJob(parameters) {

    constructor(queue: String) : this(
        Parameters.Builder()
            .setQueue(queue)
            .build()
    )

    override fun onRun() {
        Log.i(TAG, String.format("Marker reached in %s queue", parameters.queue))
    }

    override fun onShouldRetry(e: Exception): Boolean {
        return false
    }

    override fun serialize(): ByteArray? {
        return null
    }

    override fun getFactoryKey(): String {
        return KEY
    }

    override fun onFailure() {
    }

    companion object {
        private val TAG = Log.tag(MarkerJob::class.java)

        const val KEY: String = "MarkerJob"

        class Factory : Job.Factory<MarkerJob> {
            override fun create(parameters: Parameters, serializedData: ByteArray?): MarkerJob {
                return MarkerJob(parameters)
            }
        }
    }
}