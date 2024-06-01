package org.thoughtcrime.securesms.jobmanager.persistence

import androidx.annotation.WorkerThread

interface JobStorage {

    @WorkerThread
    fun init()

    @WorkerThread
    fun insertJobs(fullSpecs: List<FullSpec>)

    @WorkerThread
    fun getJobSpec(id: String): JobSpec

    @WorkerThread
    fun getAllJobSpecs(): List<JobSpec>

    @WorkerThread
    fun getPendingJobsWithNoDependenciesInCreatedOrder(currentTime: Long): List<JobSpec>

    @WorkerThread
    fun getJobsInQueue(queue: String): List<JobSpec>

    @WorkerThread
    fun getJobCountForFactory(factoryKey: String): Int

    @WorkerThread
    fun getJobCountForFactoryAndQueue(factoryKey: String, queueKey: String): Int

    @WorkerThread
    fun areQueuesEmpty(queueKeys: Set<String>): Boolean

    @WorkerThread
    fun markJobAsRunning(id: String, currentTime: Long)

    @WorkerThread
    fun updateJobAfterRetry(
        id: String,
        currentTime: Long,
        runAttempt: Int,
        nextBackoffInterval: Long,
        serializedData: ByteArray?
    )

    @WorkerThread
    fun updateAllJobsToBePending()


    @WorkerThread
    fun updateJobs(jobSpecs: List<JobSpec>)

    @WorkerThread
    fun deleteJob(id: String)

    @WorkerThread
    fun deleteJobs(ids: List<String>)

    @WorkerThread
    fun getConstraintSpecs(jobId: String): List<ConstraintSpec>

    @WorkerThread
    fun getAllConstraintSpecs(): List<ConstraintSpec>

    @WorkerThread
    fun getDependencySpecsThatDependOnJob(jobSpecId: String): List<DependencySpec>

    @WorkerThread
    fun getAllDependencySpecs(): List<DependencySpec>
}