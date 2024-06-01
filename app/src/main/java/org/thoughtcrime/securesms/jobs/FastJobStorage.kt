package org.thoughtcrime.securesms.jobs

import org.thoughtcrime.securesms.database.JobDatabase
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.persistence.ConstraintSpec
import org.thoughtcrime.securesms.jobmanager.persistence.DependencySpec
import org.thoughtcrime.securesms.jobmanager.persistence.FullSpec
import org.thoughtcrime.securesms.jobmanager.persistence.JobSpec
import org.thoughtcrime.securesms.jobmanager.persistence.JobStorage


class FastJobStorage(private val jobDatabase: JobDatabase) : JobStorage {

    val fullSpec1 = FullSpec(
        jobSpec(
            id = "1",
            factoryKey = "f1",
            queueKey = "q",
            createTime = 1,
            priority = Job.Parameters.PRIORITY_LOW
        ),
        emptyList(), emptyList()
    )
    val fullSpec2 = FullSpec(
        jobSpec(
            id = "2",
            factoryKey = "f2",
            queueKey = "q",
            createTime = 2,
            priority = Job.Parameters.PRIORITY_HIGH
        ), emptyList(), emptyList()
    )
    val fullSpec3 = FullSpec(
        jobSpec(
            id = "3",
            factoryKey = "f3",
            queueKey = "q",
            createTime = 3,
            priority = Job.Parameters.PRIORITY_DEFAULT
        ), emptyList(), emptyList()
    )

    val fullSpecList = listOf(fullSpec1, fullSpec2, fullSpec3)

    override fun init() {
        TODO("Not yet implemented")
    }

    override fun insertJobs(fullSpecs: List<FullSpec>) {
        TODO("Not yet implemented")
    }

    override fun getJobSpec(id: String): JobSpec {
        TODO("Not yet implemented")
    }

    override fun getAllJobSpecs(): List<JobSpec> {
        TODO("Not yet implemented")
    }

    override fun getPendingJobsWithNoDependenciesInCreatedOrder(currentTime: Long): List<JobSpec> {
        return fullSpecList.map { it.jobSpec }
    }

    override fun getJobsInQueue(queue: String): List<JobSpec> {
        TODO("Not yet implemented")
    }

    override fun getJobCountForFactory(factoryKey: String): Int {
        TODO("Not yet implemented")
    }

    override fun getJobCountForFactoryAndQueue(factoryKey: String, queueKey: String): Int {
        TODO("Not yet implemented")
    }

    override fun areQueuesEmpty(queueKeys: Set<String>): Boolean {
        TODO("Not yet implemented")
    }

    override fun markJobAsRunning(id: String, currentTime: Long) {
        TODO("Not yet implemented")
    }

    override fun updateJobAfterRetry(
        id: String,
        currentTime: Long,
        runAttempt: Int,
        nextBackoffInterval: Long,
        serializedData: ByteArray?
    ) {
        TODO("Not yet implemented")
    }

    override fun updateAllJobsToBePending() {
        TODO("Not yet implemented")
    }

    override fun updateJobs(jobSpecs: List<JobSpec>) {
        TODO("Not yet implemented")
    }

    override fun deleteJob(id: String) {
        TODO("Not yet implemented")
    }

    override fun deleteJobs(ids: List<String>) {
        TODO("Not yet implemented")
    }

    override fun getConstraintSpecs(jobId: String): List<ConstraintSpec> {
        TODO("Not yet implemented")
    }

    override fun getAllConstraintSpecs(): List<ConstraintSpec> {
        TODO("Not yet implemented")
    }

    override fun getDependencySpecsThatDependOnJob(jobSpecId: String): List<DependencySpec> {
        TODO("Not yet implemented")
    }

    override fun getAllDependencySpecs(): List<DependencySpec> {
        TODO("Not yet implemented")
    }


    private fun jobSpec(
        id: String,
        factoryKey: String,
        queueKey: String? = null,
        createTime: Long = 1,
        lastRunAttemptTime: Long = 1,
        nextBackoffInterval: Long = 0,
        runAttempt: Int = 1,
        maxAttempts: Int = 1,
        lifespan: Long = 1,
        serializedData: ByteArray? = null,
        serializedInputData: ByteArray? = null,
        isRunning: Boolean = false,
        isMemoryOnly: Boolean = false,
        priority: Int = 0
    ): JobSpec {
        return JobSpec(
            id = id,
            factoryKey = factoryKey,
            queueKey = queueKey,
            createTime = createTime,
            lastRunAttemptTime = lastRunAttemptTime,
            nextBackoffInterval = nextBackoffInterval,
            runAttempt = runAttempt,
            maxAttempts = maxAttempts,
            lifespan = lifespan,
            serializedData = serializedData,
            serializedInputData = serializedInputData,
            isRunning = isRunning,
            isMemoryOnly = isMemoryOnly,
            priority = priority
        )
    }
}

