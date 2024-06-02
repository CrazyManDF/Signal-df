package org.thoughtcrime.securesms.jobs

import org.thoughtcrime.securesms.database.JobDatabase
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraint
import org.thoughtcrime.securesms.jobmanager.persistence.ConstraintSpec
import org.thoughtcrime.securesms.jobmanager.persistence.DependencySpec
import org.thoughtcrime.securesms.jobmanager.persistence.FullSpec
import org.thoughtcrime.securesms.jobmanager.persistence.JobSpec
import org.thoughtcrime.securesms.jobmanager.persistence.JobStorage
import kotlin.time.Duration.Companion.minutes


class FastJobStorage(private val jobDatabase: JobDatabase) : JobStorage {

    private val jobs: MutableList<JobSpec> = mutableListOf()
    private val constraintsByJobId: MutableMap<String, MutableList<ConstraintSpec>> = mutableMapOf()
    private val dependenciesByJobId: MutableMap<String, MutableList<DependencySpec>> =
        mutableMapOf()

    val fullSpec1 = FullSpec(
        jobSpec(
            id = "1",
            factoryKey = MessageFetchJob.KEY,
            queueKey = "q",
            createTime = System.currentTimeMillis(),
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

    val fullSpecList = listOf(fullSpec1)

    @Synchronized
    override fun init() {
//        jobs += fullSpecList[0].jobSpec
    }

    @Synchronized
    override fun insertJobs(fullSpecs: List<FullSpec>) {
        val durable = fullSpecs.filterNot { it.isMemoryOnly }

        if (durable.isNotEmpty()){
//            jobDatabase.insertJobs(durable)
        }

        for (fullSpec in fullSpecs) {
            jobs += fullSpec.jobSpec
            constraintsByJobId[fullSpec.jobSpec.id] = fullSpec.constraintSpecs.toMutableList()
            dependenciesByJobId[fullSpec.jobSpec.id] = fullSpec.dependencySpecs.toMutableList()
        }
    }

    @Synchronized
    override fun getJobSpec(id: String): JobSpec {
        return fullSpecList.map { it.jobSpec }.first { it.id == id }
    }

    @Synchronized
    override fun getAllJobSpecs(): List<JobSpec> {
        return emptyList()
    }

    @Synchronized
    override fun getPendingJobsWithNoDependenciesInCreatedOrder(currentTime: Long): List<JobSpec> {
      return  jobs.groupBy {
            it.queueKey ?: it.id
        }.asSequence().map { byQueueKey ->
            byQueueKey.value
                .sortedBy { it.createTime }
                .maxByOrNull { it.priority }
        }.filterNotNull().filter { job ->
            dependenciesByJobId[job.id].isNullOrEmpty()
        }.filterNot {
            it.isRunning
        }.filter { job ->
            job.hasEligibleRunTime(currentTime)
        }.sortedBy {
            it.createTime
        }.sortedByDescending {
            it.priority
        }.toList()
    }

    @Synchronized
    override fun getJobsInQueue(queue: String): List<JobSpec> {
        return emptyList()
    }

    override fun getJobCountForFactory(factoryKey: String): Int {
        return -1
    }

    override fun getJobCountForFactoryAndQueue(factoryKey: String, queueKey: String): Int {
        return -1
    }

    override fun areQueuesEmpty(queueKeys: Set<String>): Boolean {
        return true
    }

    @Synchronized
    override fun markJobAsRunning(id: String, currentTime: Long) {
        val job = getJobById(id)
        if (job == null || !job.isMemoryOnly){
            jobDatabase.markJobAsRunning(id, currentTime)
        }

        val iter = jobs.listIterator()

        while (iter.hasNext()){
            val current = iter.next()
            if (current.id == id){
                iter.set(current.copy(isRunning = true, lastRunAttemptTime = currentTime))
            }
        }
    }

    override fun updateJobAfterRetry(
        id: String,
        currentTime: Long,
        runAttempt: Int,
        nextBackoffInterval: Long,
        serializedData: ByteArray?
    ) {

    }

    override fun updateAllJobsToBePending() {

    }

    override fun updateJobs(jobSpecs: List<JobSpec>) {

    }

    @Synchronized
    override fun deleteJob(jobIds: String) {
        deleteJobs(listOf(jobIds))
    }

    @Synchronized
    override fun deleteJobs(jobIds: List<String>) {
        val durableIds = jobIds
            .mapNotNull { getJobById(it) }
            .filterNot { it.isMemoryOnly }
            .map { it.id }

        if (durableIds.isNotEmpty()) {
//            jobDatabase.deleteJobs(durableIds)
        }
        val deleteIds = jobIds.toSet()
        jobs.removeIf { deleteIds.contains(it.id) }

        for (jobId in jobIds) {
            constraintsByJobId.remove(jobId)
            dependenciesByJobId.remove(jobId)

            for (dependencyList in dependenciesByJobId.values) {
                val iter = dependencyList.iterator()

                while (iter.hasNext()) {
                    if (iter.next().dependsOnJobId == jobId) {
                        iter.remove()
                    }
                }
            }
        }
    }

    @Synchronized
    override fun getConstraintSpecs(jobId: String): List<ConstraintSpec> {
        return listOf(
            ConstraintSpec(fullSpec1.jobSpec.id, NetworkConstraint.KEY, true)
        )
    }

    override fun getAllConstraintSpecs(): List<ConstraintSpec> {
        return emptyList()
    }

    private fun getJobById(jobId: String): JobSpec? {
        return jobs.firstOrNull { it.id == jobId }
    }

    override fun getDependencySpecsThatDependOnJob(jobSpecId: String): List<DependencySpec> {
        return emptyList()
    }

    override fun getAllDependencySpecs(): List<DependencySpec> {
        return emptyList()
    }


    private fun JobSpec.hasEligibleRunTime(currentTime: Long): Boolean {
        return this.lastRunAttemptTime > currentTime ||
                (this.lastRunAttemptTime + this.nextBackoffInterval) < currentTime
    }

    private fun jobSpec(
        id: String,
        factoryKey: String,
        queueKey: String? = null,
        createTime: Long = System.currentTimeMillis(), // 创建时间
        lastRunAttemptTime: Long = 1,
        nextBackoffInterval: Long = 0,
        runAttempt: Int = 1,
        maxAttempts: Int = 1,
        lifespan: Long = 1.minutes.inWholeMilliseconds, // 生命周期时间
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

