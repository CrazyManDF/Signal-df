package org.thoughtcrime.securesms.jobs

import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.JobLogger
import org.thoughtcrime.securesms.jobmanager.JobLogger.format
import org.thoughtcrime.securesms.jobmanager.impl.BackoffUtil
import org.thoughtcrime.securesms.util.FeatureFlags

abstract class BaseJob(parameters: Parameters) : Job(parameters) {

    private val outputData: ByteArray? = null

    override fun run(): Result {
        if (shouldTrace()) {
            // TODO:
//            Tracer.getInstance().start(javaClass.simpleName)
        }

        val result = runCatching {
            onRun()
            Result.success(outputData)
        }.getOrElse { e ->
            when (e) {
                is RuntimeException -> {
                    Log.e(TAG, "Encountered a fatal exception. Crash imminent.", e)
                    Result.fatalFailure(e)
                }

                is Exception -> {
                    if (onShouldRetry(e)) {
                        Log.i(TAG, JobLogger.format(this, "Encountered a retryable exception."), e)
                        Result.retry(getNextRunAttemptBackoff(runAttempt + 1, e))
                    } else {
                        Log.w(TAG, format(this, "Encountered a failing exception."), e)
                        Result.failure()
                    }
                }

                else -> {
                    Log.w(TAG, format(this, "Encountered a failing exception."), e)
                    Result.failure()
                }
            }
        }
        if (shouldTrace()) {
//              Tracer.getInstance().end(javaClass.simpleName)
        }
        return result
    }

    fun getNextRunAttemptBackoff(pastAttemptCount: Int, exception: Exception): Long {
        return BackoffUtil.exponentialBackoff(pastAttemptCount, FeatureFlags.getDefaultMaxBackoff())
    }

    @Throws(Exception::class)
    protected abstract fun onRun()

    protected abstract fun onShouldRetry(e: Exception): Boolean

    protected fun shouldTrace(): Boolean {
        return false
    }

    companion object {
        private val TAG = Log.tag(BaseJob::class.java)
    }
}