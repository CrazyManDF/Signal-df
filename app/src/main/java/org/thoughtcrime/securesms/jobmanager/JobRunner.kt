package org.thoughtcrime.securesms.jobmanager

import android.app.Application
import android.os.PowerManager
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.util.WakeLockUtil
import java.util.concurrent.TimeUnit

class JobRunner<T : Job, U : Constraint>(
    private val application: Application,
    private val id: Int,
    private val jobController: JobController<T, U>,
    private val predicate: JobPredicate
) : Thread("signal-JobRunner-$id") {

    @Synchronized
    override fun run() {
        super.run()
        while (true) {
            val job = jobController.pullNextEligibleJobForExecution(predicate)
            val result = run(job)

            jobController.onJobFinished(job)

            if (result.isSuccess()) {
                jobController.onSuccess(job, result.outputData)
            } else if (result.isRetry()) {
                jobController.onRetry(job, result.backoffInterval)
                job.onRetry()
            } else if (result.isFailure()) {
                val dependents = jobController.onFailure(job)
                job.onFailure()
                dependents.forEach { it.onFailure() }

                result.getException()?.let {
                    throw it
                }
            } else {
                throw AssertionError("Invalid job result!")
            }
        }
    }

    private fun run(job: Job): Job.Result {
        val runStartTime = System.currentTimeMillis()
        Log.i(TAG, JobLogger.format(job, id.toString(), "Running job."))

        if (isJobExpired(job)) {
            Log.w(TAG, JobLogger.format(job, id.toString(), "Failing after surpassing its lifespan."))
            return Job.Result.failure()
        }
        var result: Job.Result? = null
        var wakeLock: PowerManager.WakeLock? = null

        runCatching {
            wakeLock = WakeLockUtil.acquire(application, PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TIMEOUT, job.getId())

            result = job.run()

            if (job.isCanceled()) {
                Log.w(TAG, JobLogger.format(job, id.toString(), "Failing because the job was canceled."))
                result = Job.Result.failure()
            }
        }.onFailure { e ->
            Log.w(TAG, JobLogger.format(job, id.toString(), "Failing due to an unexpected exception."), e)
            return Job.Result.failure()
        }
        if (wakeLock != null) {
            WakeLockUtil.release(wakeLock, job.getId())
        }

        printResult(job, result!!, runStartTime)

        if (result!!.isRetry()                                  &&
            job.runAttempt + 1 >= job.parameters.maxAttempts    &&
            job.parameters.maxAttempts != Job.Parameters.UNLIMITED
        ) {
            Log.w(TAG, JobLogger.format(job, id.toString(), "Failing after surpassing its max number of attempts."))
            return Job.Result.failure()
        }

        return result!!
    }

    private fun isJobExpired(job: Job): Boolean {
        var expirationTime = job.parameters.createTime + job.parameters.lifespan
        if (expirationTime < 0) expirationTime = Long.MAX_VALUE

        return job.parameters.lifespan != Job.Parameters.IMMORTAL &&
                expirationTime <= System.currentTimeMillis()
    }

    private fun printResult(job: Job, result: Job.Result, runStartTime: Long) {
        if (result.getException() != null) {
            Log.e(TAG, JobLogger.format(job, id.toString(), "Job failed with a fatal exception. Crash imminent."))
        } else if (result.isFailure()) {
            Log.w(TAG, JobLogger.format(job, id.toString(), "Job failed."))
        } else {
            Log.i(TAG, JobLogger.format(job, id.toString(), "Job finished with result " + result + " in " + (System.currentTimeMillis() - runStartTime) + " ms."))
        }
    }

    companion object {
        private val TAG = Log.tag(JobRunner::class.java)

        private val WAKE_LOCK_TIMEOUT = TimeUnit.MINUTES.toMillis(10)
    }
}