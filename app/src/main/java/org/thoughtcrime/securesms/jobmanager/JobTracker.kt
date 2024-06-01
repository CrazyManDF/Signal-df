package org.thoughtcrime.securesms.jobmanager

import android.app.job.JobInfo
import androidx.annotation.AnyThread
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.signal.core.util.concurrent.SignalExecutors
import org.thoughtcrime.securesms.util.LRUCache
import java.util.concurrent.Executor

class JobTracker() {

    private val jobInfos: LRUCache<String, JobInfo> = LRUCache(100)
    private val jobListeners: ArrayList<ListenerInfo> = arrayListOf()
    private val listenerExecutor: Executor = SignalExecutors.BOUNDED


    @Synchronized
    fun addListener(filter: JobFilter, listener: JobListener) {
        jobListeners.add(ListenerInfo(filter, listener))
    }

    @Synchronized
    fun removeListener(listener: JobListener) {
        val iterator = jobListeners.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().listener == listener) {
                iterator.remove()
            }
        }
    }

    @Synchronized
    fun getFirstMatchingJobState(jobFilter: JobFilter): JobState? {
        jobInfos.values.forEach { info ->
            if (jobFilter.matches(info.job)) {
                return info.jobState
            }
        }
        return null
    }

    @Synchronized
    fun onStateChange(job: Job, jobState: JobState) {
        getOrCreateJobInfo(job).jobState = jobState

        runBlocking {
            jobListeners.asFlow()
                .filter {
                    it.filter.matches(job)
                }.map {
                    it.listener
                }.collect {
                    listenerExecutor.execute { it.onStateChanged(job, jobState) }
                }
        }
    }

    @Synchronized
    fun haveAnyFailed(jobIds: Collection<String>): Boolean {
        jobIds.forEach { jobId ->
            val jobInfo = jobInfos[jobId]
            if (jobInfo != null && jobInfo.jobState == JobState.FAILURE) {
                return true
            }
        }
        return false
    }

    private fun getOrCreateJobInfo(job: Job): JobInfo {
        var jobInfo = jobInfos[job.getId()]

        if (jobInfo == null) {
            jobInfo = JobInfo(job)
        }
        jobInfos[job.getId()] = jobInfo
        return jobInfo
    }

    interface JobFilter {
        fun matches(job: Job): Boolean
    }

    interface JobListener {
        @AnyThread
        fun onStateChanged(job: Job, jobState: JobState)
    }

    enum class JobState(val complete: Boolean) {
        PENDING(false),
        RUNNING(false),
        SUCCESS(true),
        FAILURE(true),
        IGNORED(true);

        fun isComplete() = complete
    }

    class ListenerInfo(val filter: JobFilter, val listener: JobListener)
    class JobInfo(val job: Job) {
        var jobState: JobState? = null
    }
}