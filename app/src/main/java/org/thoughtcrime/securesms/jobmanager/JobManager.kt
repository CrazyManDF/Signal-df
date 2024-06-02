package org.thoughtcrime.securesms.jobmanager

import android.app.Application
import android.os.Build
import androidx.annotation.GuardedBy
import androidx.annotation.VisibleForTesting
import org.signal.core.util.ThreadUtil
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.jobmanager.JobTracker.JobFilter
import org.thoughtcrime.securesms.jobmanager.JobTracker.JobListener
import org.thoughtcrime.securesms.jobmanager.impl.DefaultExecutorFactory
import org.thoughtcrime.securesms.jobmanager.persistence.JobStorage
import org.thoughtcrime.securesms.util.Debouncer
import org.thoughtcrime.securesms.util.Util
import org.thoughtcrime.securesms.util.concurrent.FilteredExecutor
import java.util.LinkedList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executor
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.math.min

class JobManager(
    val application: Application,
    val configuration: Configuration
) : ConstraintObserver.Notifier {

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    @GuardedBy("emptyQueueListeners")
    private val emptyQueueListeners = CopyOnWriteArraySet<EmptyQueueListener>()

    @Volatile
    private var initialized: Boolean = false

    private val executor: Executor =
        FilteredExecutor(configuration.executorFactory.newSingleThreadExecutor("signal-JobManager"),
            object : FilteredExecutor.Filter {
                override fun shouldRunOnExecutor(): Boolean {
                    return ThreadUtil.isMainThread()
                }
            })
    private val jobTracker = configuration.jobTracker
    private val jobController = JobController(
        application,
        configuration.jobStorage,
        configuration.jobInstantiator,
        configuration.constraintInstantiator,
        configuration.jobTracker,
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            AlarmManagerScheduler(application)
        else
            CompositeScheduler(arrayOf(InAppScheduler(this), JobSchedulerScheduler(application))),
        Debouncer(500),
        object : JobController.Callback {
            override fun onEmpty() {
                onEmptyQueue()
            }
        }
    )

    init {
        executor.execute {
            lock.withLock {
                val jobStorage = configuration.jobStorage
                jobStorage.init()

                // TODO: 未完成
                //                    int latestVersion = configuration.getJobMigrator().migrate(jobStorage);
                //                    TextSecurePreferences.setJobManagerVersion(application, latestVersion);

                jobController.init()
                configuration.constraintObservers.forEach {
                    it.register(this@JobManager)
                }
                //                    if (Build.VERSION.SDK_INT < 26) {
                //                        application.startService(Intent(application, KeepAliveService.class))
                //                    }

                initialized = true

                condition.signalAll()
                jobController.wakeUp()
            }
        }
    }

    /**
     * 开始执行Job
     */
    fun beginJobLoop() {
        runOnExecutor {
            var id = 0
            for (i in 0..<configuration.jobThreadCount) {
                JobRunner(application, ++id, jobController, JobPredicate.NONE).start()
            }
            configuration.reservedJobRunners.forEach { predicate ->
                JobRunner(application, ++id, jobController, predicate).start()
            }

            jobController.wakeUp()
        }
    }

    fun addListener(id: String, listener: JobListener) {
        jobTracker.addListener(JobIdFilter(id), listener)
    }

    fun addListener(filter: JobFilter, listener: JobListener) {
        jobTracker.addListener(filter, listener)
    }

    fun removeListener(listener: JobListener) {
        jobTracker.removeListener(listener)
    }

    fun getFirstMatchingJobState(filter: JobFilter): JobTracker.JobState? {
        return jobTracker.getFirstMatchingJobState(filter)
    }

    fun add(job: Job) {
        Chain(this, listOf(job)).enqueue()
    }

    private fun runOnExecutor(runnable: Runnable) {
        executor.execute {
            waitUntilInitialized()
            runnable.run()
        }
    }

    private fun waitUntilInitialized() {
        if (initialized.not()) {
            Log.i(TAG, "等待初始化 Waiting for initialization...")
            lock.withLock {
                while (initialized.not()) {
                    Util.wait(condition, 0)
                }
            }
            Log.i(TAG, "初始化完成 Initialization complete.")
        }
    }

    private fun onEmptyQueue() {

    }

    class JobIdFilter(private val id: String) : JobFilter {
        override fun matches(job: Job): Boolean {
            return id == job.getId()
        }
    }

    class Configuration private constructor(
        var jobThreadCount: Int,
        var executorFactory: ExecutorFactory,
        var jobInstantiator: JobInstantiator<*>,
        var constraintInstantiator: ConstraintInstantiator<*>,
        var constraintObservers: List<ConstraintObserver> = mutableListOf(),
        var jobStorage: JobStorage,
        var jobMigrator: JobMigrator?,
        var jobTracker: JobTracker,
        var reservedJobRunners: List<JobPredicate> = mutableListOf()
    ) {

        class Builder<T : Job, U : Constraint> {
            var executorFactory: ExecutorFactory = DefaultExecutorFactory()
            var jobThreadCount: Int = max(2, min(4, Runtime.getRuntime().availableProcessors() - 1))
            var jobFactories: Map<String, Job.Factory<T>> = hashMapOf()
            var constraintFactories: Map<String, Constraint.Factory<U>> = hashMapOf()
            var constraintObservers: List<ConstraintObserver> = mutableListOf()
            var jobStorage: JobStorage? = null
            var jobMigrator: JobMigrator? = null
            private var jobTracker: JobTracker = JobTracker()
            var reservedJobRunners = mutableListOf<JobPredicate>()

            fun setJobThreadCount(jobThreadCount: Int) = apply {
                this.jobThreadCount = jobThreadCount
            }

            fun addReservedJobRunner(predicate: JobPredicate) = apply {
                this.reservedJobRunners.add(predicate)
            }

            fun setExecutorFactory(executorFactory: ExecutorFactory) = apply {
                this.executorFactory = executorFactory
            }

            fun setJobFactories(jobFactories: Map<String, Job.Factory<T>>) = apply {
                this.jobFactories = jobFactories
            }

            fun setConstraintFactories(constraintFactories: Map<String, Constraint.Factory<U>>) =
                apply {
                    this.constraintFactories = constraintFactories
                }

            fun setConstraintObservers(constraintObservers: List<ConstraintObserver>) = apply {
                this.constraintObservers = constraintObservers
            }

            fun setJobStorage(jobStorage: JobStorage) = apply {
                this.jobStorage = jobStorage
            }

            fun setJobMigrator(jobMigrator: JobMigrator) = apply {
                this.jobMigrator = jobMigrator
            }

            fun build(): Configuration {
                return Configuration(
                    jobThreadCount,
                    executorFactory,
                    JobInstantiator(jobFactories),
                    ConstraintInstantiator(constraintFactories),
                    constraintObservers,
                    jobStorage!!,
                    jobMigrator,
                    jobTracker,
                    reservedJobRunners
                )
            }
        }
    }

    interface EmptyQueueListener {
        fun onQueueEmpty()
    }

    class Chain(
        private val jobManager: JobManager,
        jobs: List<Job> = LinkedList<Job>()
    ) {

        private val jobList = LinkedList(arrayListOf(jobs))

        fun then(job: Job): Chain {
            return then(listOf(job))
        }

        fun then(jobs: List<Job>): Chain {
            if (jobs.isNotEmpty()) {
                this.jobList.add(jobs)
            }
            return this
        }

        fun after(job: Job): Chain {
            return after(listOf(job))
        }

        fun after(jobs: List<Job>): Chain {
            if (jobs.isNotEmpty()) {
                this.jobList.add(0, jobs)
            }
            return this
        }

        fun enqueue() {
            jobManager.enqueueChain(this)
        }

        @VisibleForTesting
        fun getJobListChain(): List<List<Job>> {
            return jobList
        }
    }

    override fun onConstraintMet(reason: String) {
        if (!initialized) {
            Log.d(TAG, "Ignoring early onConstraintMet($reason)")
            return
        }

        Log.i(TAG, "onConstraintMet($reason)")
        wakeUp()
    }

    fun wakeUp() {
        runOnExecutor { jobController.wakeUp() }
    }

    private fun enqueueChain(chain: Chain) {
        for (jobList in chain.getJobListChain()) {
            for (job in jobList) {
                jobTracker.onStateChange(job, JobTracker.JobState.PENDING)
            }
        }

        runOnExecutor {
            jobController.submitNewJobChain(chain.getJobListChain())
            jobController.wakeUp()
        }
    }

    companion object {

        private val TAG = Log.tag(JobManager::class.java)

        const val CURRENT_VERSION: Int = 11
    }
}