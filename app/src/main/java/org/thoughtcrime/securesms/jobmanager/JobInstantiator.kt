package org.thoughtcrime.securesms.jobmanager

class JobInstantiator<T : Job>(
    private val jobFactories: Map<String, Job.Factory<T>>
) {

    fun instantiate(jobFactoryKey: String, parameters: Job.Parameters, data: ByteArray?): Job {
        val factory = jobFactories[jobFactoryKey]
        if (factory != null) {
            val job = factory.create(parameters, data)

            if (job.getId() != parameters.id) {
                throw AssertionError("Parameters not supplied to job during creation")
            }

            return job
        } else {
            throw IllegalStateException("Tried to instantiate a job with key '$jobFactoryKey', but no matching factory was found.")
        }
    }
}