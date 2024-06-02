package org.thoughtcrime.securesms.jobmanager

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.core.util.Predicate
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.jobmanager.persistence.ConstraintSpec
import org.thoughtcrime.securesms.jobmanager.persistence.DependencySpec
import org.thoughtcrime.securesms.jobmanager.persistence.FullSpec
import org.thoughtcrime.securesms.jobmanager.persistence.JobSpec
import org.thoughtcrime.securesms.jobmanager.persistence.JobStorage
import org.thoughtcrime.securesms.util.Debouncer
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class JobController<T : Job, U : Constraint>(
    private val application: Application,
    private val jobStorage: JobStorage,
    private val jobInstantiator: JobInstantiator<T>,
    private val constraintInstantiator: ConstraintInstantiator<U>,
    private val jobTracker: JobTracker,
    private val scheduler: Scheduler,
    private val debouncer: Debouncer,
    private val callback: Callback,
) {

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private val runningJobs = hashMapOf<String, Job>()

    @WorkerThread
    fun init() = lock.withLock {
        jobStorage.updateAllJobsToBePending()
        condition.signalAll()
    }

    fun wakeUp() = lock.withLock {
        condition.signalAll()
    }

    @WorkerThread
    fun submitNewJobChains(chains: List<List<List<Job>>>) = lock.withLock {
        chains.forEach {
            submitNewJobChain(it)
        }
    }

    @WorkerThread
    fun submitNewJobChain(chainValue: List<List<Job>>) {
        val chain = lock.withLock {
            val chain = chainValue.filterNot { it.isEmpty() }
            if (chain.isEmpty()) {
                Log.w(TAG, "Tried to submit an empty job chain. Skipping.");
                return
            }
            if (chainExceedsMaximumInstances(chain)) {
                val solo = chain[0][0]
                jobTracker.onStateChange(solo, JobTracker.JobState.IGNORED)
                Log.w(
                    TAG,
                    JobLogger.format(
                        solo,
                        "Already at the max instance count. Factory limit: " + solo.parameters.maxInstancesForFactory + ", Queue limit: " + solo.parameters.maxInstancesForQueue + ". Skipping."
                    )
                )
                return
            }

            insertJobChain(chain)
            scheduleJobs(chain[0])
            chain
        }

        triggerOnSubmit(chain)

        lock.withLock {
            condition.signalAll()
        }
    }

    @WorkerThread
    fun submitJobWithExistingDependencies(
        job: Job,
        dependsOn: Collection<String>,
        dependsOnQueue: String?
    ) {

    }

    @WorkerThread
    fun submitJobs(jobs: List<Job>) {
        val canRun = mutableListOf<Job>()

        lock.withLock {
            jobs.forEach { job ->
                if (exceedsMaximumInstances(job)) {
                    jobTracker.onStateChange(job, JobTracker.JobState.IGNORED);
                    Log.w(
                        TAG,
                        JobLogger.format(
                            job,
                            "Already at the max instance count. Factory limit: " + job.parameters.maxInstancesForFactory + ", Queue limit: " + job.parameters.maxInstancesForQueue + ". Skipping."
                        )
                    )
                } else {
                    canRun.add(job)
                }
            }

            if (canRun.isEmpty()) {
                return
            }

            val fullSpecs = runBlocking {
                canRun.asFlow().map { buildFullSpec(it, emptyList()) }.toList()
            }
            jobStorage.insertJobs(fullSpecs)

            scheduleJobs(canRun)
        }

        canRun.forEach { job ->
            job.overwriteContext(application)
            job.onSubmit()
        }

        lock.withLock {
            condition.signalAll()
        }
    }

    @WorkerThread
    fun cancelJob(id: String) = lock.withLock {
        val runningJob = runningJobs[id]

        if (runningJob != null) {
            Log.w(TAG, JobLogger.format(runningJob, "Canceling while running."))
            runningJob.cancel()
        } else {
            val jobSpec = jobStorage.getJobSpec(id)

            if (jobSpec != null) {
                val job = createJob(jobSpec, jobStorage.getConstraintSpecs(id))
                Log.w(TAG, JobLogger.format(job, "Canceling while inactive."))
                Log.w(TAG, JobLogger.format(job, "Job failed."))

                job.cancel()
                val dependents = onFailure(job)
                job.onFailure()
                runBlocking {
                    dependents.asFlow().collect { it.onFailure() }
                }
            } else {
                Log.w(TAG, "Tried to cancel JOB::$id, but it could not be found.")
            }
        }
    }

    @WorkerThread
    fun cancelAllInQueue(queue: String) = lock.withLock {

    }

//    @WorkerThread
//    fun update(updater: JobUpdater) = lock.withLock {
//
//    }


    @WorkerThread
    fun findJobs(predicate: Predicate<JobSpec>): List<JobSpec> = lock.withLock {
        jobStorage.getAllJobSpecs().filter { predicate.test(it) }
    }

    @WorkerThread
    fun onRetry(job: Job, backoffInterval: Long) = lock.withLock {
        require(backoffInterval > 0) { "Invalid backoff interval! $backoffInterval" }

        Log.d(TAG, "执行重试job ${job.getId()}  ${Thread.currentThread()}")
        val nextRunAttempt = job.runAttempt + 1
        val serializedData = job.serialize()

        jobStorage.updateJobAfterRetry(
            job.getId(),
            System.currentTimeMillis(),
            nextRunAttempt,
            backoffInterval,
            serializedData
        )
        jobTracker.onStateChange(job, JobTracker.JobState.PENDING)

        val constraints = jobStorage.getConstraintSpecs(job.getId())
            .map { it.factoryKey }
            .map { constraintInstantiator.instantiate(it) }
        Log.i(TAG, JobLogger.format(job, "Scheduling a retry in $backoffInterval ms."))
        scheduler.schedule(backoffInterval, constraints)

        condition.signalAll()
    }

    @WorkerThread
    fun onJobFinished(job: Job) = lock.withLock {
        runningJobs.remove(job.getId())
    }

    @WorkerThread
    fun onSuccess(job: Job, outputData: ByteArray?) = lock.withLock {
        Log.d(TAG, "执行成功job ${job?.getId()}  ${Thread.currentThread()}")
        if (outputData != null) {
            val updates = jobStorage.getDependencySpecsThatDependOnJob(job.getId())
                .map { it.jobId }
                .map { jobStorage.getJobSpec(it) }
                .map { mapToJobWithInputData(it, outputData) }

            jobStorage.updateJobs(updates)
        }

        jobStorage.deleteJob(job.getId())
        jobTracker.onStateChange(job, JobTracker.JobState.SUCCESS)
        condition.signalAll()
    }

    /**
     * @return The list of all dependent jobs that should also be failed.
     */
    @WorkerThread
    fun onFailure(job: Job): List<Job> = lock.withLock {
        Log.d(TAG, "执行失败job ${job.getId()}  ${Thread.currentThread()}")
        val dependents = jobStorage.getDependencySpecsThatDependOnJob(job.getId())
            .map { it.jobId }
            .map { jobStorage.getJobSpec(it) }
            .filterNotNull()
            .map { jobSpec: JobSpec ->
                val constraintSpecs = jobStorage.getConstraintSpecs(jobSpec.id)
                createJob(jobSpec, constraintSpecs)
            }

        val all = mutableListOf<Job>()
        all.add(job)
        all.addAll(dependents)


        jobStorage.deleteJobs(all.map { it.getId() }.toList())
        all.forEach {
            jobTracker.onStateChange(it, JobTracker.JobState.FAILURE)
        }

        return dependents
    }

    @WorkerThread
    fun pullNextEligibleJobForExecution(predicate: JobPredicate): Job = lock.withLock {
        runCatching {
            Log.d(TAG, "进入pullNextEligibleJobForExecution==${Thread.currentThread()}")
            Log.d(TAG, "$this==$lock")
            var job: Job?

            while (getNextEligibleJobForExecution(predicate).also {
                    job = it
                } == null) {

                if (runningJobs.isEmpty()) {
                    debouncer.publish(callback::onEmpty)
                }
                Log.d(TAG, "等待-获取job ${Thread.currentThread()}")
                condition.await()
                Log.d(TAG, "唤醒-获取job ${Thread.currentThread()}")
            }

            Log.d(TAG, "获取到job ${job?.getId()}  ${Thread.currentThread()}")
            jobStorage.markJobAsRunning(job!!.getId(), System.currentTimeMillis())
            runningJobs[job!!.getId()] = job!!
            jobTracker.onStateChange(job!!, JobTracker.JobState.RUNNING)

            Log.d(TAG, "离开pullNextEligibleJobForExecution==${Thread.currentThread()}")
            job!!
        }.getOrElse { e ->
            Log.e(TAG, "Interrupted.  ${Thread.currentThread()}");
            throw AssertionError(e)
        }
    }

    @WorkerThread
    fun getDebugInfo(): String = lock.withLock {
        return "";
    }

    fun areQueuesEmpty(queueKeys: Set<String>): Boolean = lock.withLock {
        return jobStorage.areQueuesEmpty(queueKeys)
    }

    @WorkerThread
    private fun chainExceedsMaximumInstances(chain: List<List<Job>>): Boolean {
        if (chain.size == 1 && chain[0].size == 1) {
            return exceedsMaximumInstances(chain[0][0]);
        } else {
            return false
        }
    }

    @WorkerThread
    private fun exceedsMaximumInstances(job: Job): Boolean {
        val exceedsFactory = job.parameters.maxInstancesForFactory != Job.Parameters.UNLIMITED &&
                jobStorage.getJobCountForFactory(job.getFactoryKey()) >= job.parameters.maxInstancesForFactory

        if (exceedsFactory) {
            return true
        }

        val exceedsQueue = job.parameters.queue != null &&
                job.parameters.maxInstancesForQueue != Job.Parameters.UNLIMITED &&
                jobStorage.getJobCountForFactoryAndQueue(
                    job.getFactoryKey(),
                    job.parameters.queue!!
                ) >= job.parameters.maxInstancesForQueue

        return exceedsQueue
    }

    @WorkerThread
    private fun triggerOnSubmit(chain: List<List<Job>>) {
        chain.forEach { list ->
            list.forEach { job ->
                job.overwriteContext(application)
                job.onSubmit()
            }
        }
    }

    @WorkerThread
    private fun insertJobChain(chain: List<List<Job>>) {
        val fullSpecs = LinkedList<FullSpec>()
        val dependsOn = mutableListOf<String>()

        chain.forEach { jobList: List<Job> ->
            jobList.forEach { job ->
                fullSpecs.add(buildFullSpec(job, dependsOn))
            }
            // 后面的job依赖前面的job
            dependsOn.addAll(jobList.map { it.getId() })
        }

        jobStorage.insertJobs(fullSpecs)
    }

    @WorkerThread
    private fun buildFullSpec(job: Job, dependsOn: Collection<String>): FullSpec {
        job.runAttempt = 0

        val jobSpec = JobSpec(
            job.getId(),
            job.getFactoryKey(),
            job.parameters.queue,
            System.currentTimeMillis(),
            job.lastRunAttemptTime,
            job.nextBackoffInterval,
            job.runAttempt,
            job.parameters.maxAttempts,
            job.parameters.lifespan,
            job.serialize(),
            null,
            false,
            job.parameters.memoryOnly,
            job.parameters.priority
        )

        val constraintSpecs = job.parameters.constraintKeys.map {
            ConstraintSpec(jobSpec.id, it, jobSpec.isMemoryOnly)
        }

        val dependencySpecs = dependsOn.map { depends ->
            val dependsOnJobSpec = jobStorage.getJobSpec(depends)
            val memoryOnly =
                job.parameters.memoryOnly || (dependsOnJobSpec != null && dependsOnJobSpec.isMemoryOnly)

            DependencySpec(job.getId(), depends, memoryOnly)
        }

        return FullSpec(jobSpec, constraintSpecs, dependencySpecs)
    }

    @WorkerThread
    private fun scheduleJobs(jobs: List<Job>) {
        jobs.forEach { job ->
            val constraintKeys = job.parameters.constraintKeys
            val constraints: MutableList<Constraint> = mutableListOf()
            constraintKeys.forEach {
                constraints.add(constraintInstantiator.instantiate(it))
            }

            scheduler.schedule(0, constraints)
        }
    }

    @WorkerThread
    private fun getNextEligibleJobForExecution(jobPredicate: JobPredicate): Job? {
        val jobSpecs =
            jobStorage.getPendingJobsWithNoDependenciesInCreatedOrder(System.currentTimeMillis())
                .filter { jobPredicate.shouldRun(it) }

        jobSpecs.forEach { jobSpec ->
            val constraintSpecs = jobStorage.getConstraintSpecs(jobSpec.id)
            val constraints =
                constraintSpecs.map { it.factoryKey }.map { constraintInstantiator.instantiate(it) }

            if (constraints.all { it.isMet() }) {
                return createJob(jobSpec, constraintSpecs)
            }
        }
        return null
    }

    private fun createJob(jobSpec: JobSpec, constraintSpecs: List<ConstraintSpec>): Job {
        val parameters = buildJobParameters(jobSpec, constraintSpecs)
        runCatching {
            val job =
                jobInstantiator.instantiate(jobSpec.factoryKey, parameters, jobSpec.serializedData)

            job.runAttempt = jobSpec.runAttempt
            job.lastRunAttemptTime = jobSpec.lastRunAttemptTime
            job.nextBackoffInterval = jobSpec.nextBackoffInterval
            job.overwriteContext(application)

            return job
        }.getOrElse { e ->
            Log.e(
                TAG,
                "Failed to instantiate job! Failing it and its dependencies without calling Job#onFailure. Crash imminent."
            );

            val failIds = jobStorage.getDependencySpecsThatDependOnJob(jobSpec.id)
                .map { spec -> spec.jobId }


            Log.e(TAG, "Failed " + failIds.size + " dependent jobs.")
            throw e
        }
    }

    private fun buildJobParameters(
        jobSpec: JobSpec,
        constraintSpecs: List<ConstraintSpec>
    ): Job.Parameters {
        return Job.Parameters.Builder(jobSpec.id)
            .setCreateTime(jobSpec.createTime)
            .setLifespan(jobSpec.lifespan)
            .setMaxAttempts(jobSpec.maxAttempts)
            .setQueue(jobSpec.queueKey)
            .setConstraints(constraintSpecs.map { it.factoryKey })
            .setInputData(jobSpec.serializedInputData)
            .build()
    }

    private fun mapToJobWithInputData(jobSpec: JobSpec, inputData: ByteArray): JobSpec {
        return JobSpec(
            jobSpec.id,
            jobSpec.factoryKey,
            jobSpec.queueKey,
            jobSpec.createTime,
            jobSpec.lastRunAttemptTime,
            jobSpec.nextBackoffInterval,
            jobSpec.runAttempt,
            jobSpec.maxAttempts,
            jobSpec.lifespan,
            jobSpec.serializedData,
            inputData,
            jobSpec.isRunning,
            jobSpec.isMemoryOnly,
            jobSpec.priority
        )
    }

    interface Callback {
        fun onEmpty()
    }

    companion object {
        private val TAG = Log.tag(JobController::class.java)
    }
}