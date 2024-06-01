package org.thoughtcrime.securesms.jobmanager

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PACKAGE_PRIVATE
import androidx.annotation.WorkerThread
import org.signal.core.util.logging.Log
import java.util.LinkedList
import java.util.UUID
import kotlin.concurrent.Volatile

abstract class Job(val parameters: Parameters) {

    companion object {
        private val TAG = Log.tag(Job::class.java)
    }

    var runAttempt = 0

    var lastRunAttemptTime: Long = 0
    var nextBackoffInterval: Long = 0

    @Volatile
    private var canceled = false

    val maxAttempts = 0

    protected lateinit var context: Context

    fun getId(): String {
        return parameters.id
    }

    fun setContext(context: Context) {
        this.context = context
    }

    fun cancel() {
        this.canceled = true
    }

    @WorkerThread
    fun onSubmit() {
        Log.i(TAG, JobLogger.format(this, "onSubmit()"))
        onAdded()
    }

    fun isCanceled(): Boolean {
        return canceled
    }

    /**
     * Called when the job is first submitted to the {@link JobManager}.
     */
    open fun onAdded() {

    }

    /**
     * Called after a job has run and its determined that a retry is required.
     */
    open fun onRetry() {

    }


    /**
     * Serialize your job state so that it can be recreated in the future.
     */
    abstract fun serialize(): ByteArray?

    /**
     * Returns the key that can be used to find the relevant factory needed to create your job.
     */
    abstract fun getFactoryKey(): String

    /**
     * Called to do your actual work.
     */
    @WorkerThread
    abstract fun run(): Result

    /**
     * Called when your job has completely failed and will not be run again.
     */
    @WorkerThread
    abstract fun onFailure()


    interface Factory<T : Job> {
        fun create(parameters: Parameters, serializedData: ByteArray?): T
    }

    class Result(
        private val resultType: ResultType,
        val runtimeException: RuntimeException?,
        val outputData: ByteArray?,
        val backoffInterval: Long
    ) {
        companion object {

            private val INVALID_BACKOFF = -1L

            private val SUCCESS_NO_DATA: Result =
                Result(ResultType.SUCCESS, null, null, INVALID_BACKOFF)
            private val FAILURE: Result = Result(ResultType.SUCCESS, null, null, INVALID_BACKOFF)

            fun success(): Result {
                return SUCCESS_NO_DATA
            }

            fun success(outputData: ByteArray?): Result {
                return Result(ResultType.SUCCESS, null, outputData, INVALID_BACKOFF)
            }

            fun retry(backoffInterval: Long): Result {
                return Result(ResultType.RETRY, null, null, backoffInterval)
            }

            fun failure(): Result {
                return FAILURE
            }

            fun fatalFailure(runtimeException: java.lang.RuntimeException): Result {
                return Result(ResultType.FAILURE, runtimeException, null, INVALID_BACKOFF)
            }
        }

        fun isSuccess(): Boolean {
            return resultType == ResultType.SUCCESS
        }

        @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
        fun isRetry(): Boolean {
            return resultType == ResultType.RETRY
        }

        @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
        fun isFailure(): Boolean {
            return resultType == ResultType.FAILURE
        }

        fun getException(): RuntimeException? {
            return runtimeException
        }

        override fun toString(): String {
            return when (resultType) {
                ResultType.SUCCESS, ResultType.RETRY -> resultType.toString()
                ResultType.FAILURE -> {
                    if (runtimeException == null) {
                        resultType.toString()
                    } else {
                        "FATAL_FAILURE"
                    }
                }
            }
        }

        enum class ResultType {
            SUCCESS, FAILURE, RETRY
        }
    }

    class Parameters(
        var id: String,
        var createTime: Long,
        var lifespan: Long,
        var maxAttempts: Int,
        var maxInstancesForFactory: Int,
        var maxInstancesForQueue: Int,
        var queue: String?,
        var constraintKeys: MutableList<String>,
        var inputData: ByteArray?,
        var memoryOnly: Boolean,
        var priority: Int,
    ) {

        companion object {
            val MIGRATION_QUEUE_KEY: String = "MIGRATION"

            val IMMORTAL: Long = -1
            val UNLIMITED: Int = -1

            val PRIORITY_DEFAULT: Int = 0
            val PRIORITY_HIGH: Int = 1
            val PRIORITY_LOW: Int = -1
        }

        fun toBuilder(): Builder {
            return Builder(
                id,
                createTime,
                lifespan,
                maxAttempts,
                maxInstancesForFactory,
                maxInstancesForQueue,
                queue,
                constraintKeys,
                inputData,
                memoryOnly,
                priority
            )
        }

        class Builder constructor(
            var id: String,
            var createTime: Long,
            var lifespan: Long,
            var maxAttempts: Int,
            var maxInstancesForFactory: Int,
            var maxInstancesForQueue: Int,
            var queue: String?,
            var constraintKeys: MutableList<String>,
            var inputData: ByteArray?,
            var memoryOnly: Boolean,
            var priority: Int,
        ) {

            constructor() : this(UUID.randomUUID().toString())

            constructor(id: String) : this(
                id,
                System.currentTimeMillis(),
                IMMORTAL,
                1,
                UNLIMITED,
                UNLIMITED,
                null,
                LinkedList(),
                null,
                false,
                PRIORITY_DEFAULT
            )


            fun setCreateTime(createTime: Long): Builder {
                this.createTime = createTime
                return this
            }

            /**
             * Specify the amount of time this job is allowed to be retried. Defaults to {@link #IMMORTAL}.
             */
            fun  setLifespan(lifespan: Long ): Builder {
                this.lifespan = lifespan;
                return this;
            }

            /**
             * Specify the maximum number of times you want to attempt this job. Defaults to 1.
             */
            fun setMaxAttempts(maxAttempts: Int): Builder {
                this.maxAttempts = maxAttempts
                return this
            }

            /**
             * Specify the maximum number of instances you'd want of this job at any given time, as
             * determined by the job's factory key. If enqueueing this job would put it over that limit,
             * it will be ignored.
             *
             * This property is ignored if the job is submitted as part of a [JobManager.Chain].
             *
             * Defaults to [.UNLIMITED].
             */
            fun setMaxInstancesForFactory(maxInstancesForFactory: Int): Builder {
                this.maxInstancesForFactory = maxInstancesForFactory
                return this
            }

            fun setMaxInstancesForQueue(maxInstancesForQueue: Int): Builder {
                this.maxInstancesForQueue = maxInstancesForQueue
                return this
            }

            fun setQueue(queue: String?): Builder {
                this.queue = queue
                return this
            }

            fun addConstraint(constraintKey: String): Builder {
                constraintKeys.add(constraintKey)
                return this
            }

            fun setConstraints(constraintKeys: List<String>): Builder {
                this.constraintKeys.clear()
                this.constraintKeys.addAll(constraintKeys)
                return this
            }

            fun setMemoryOnly(memoryOnly: Boolean): Builder {
                this.memoryOnly = memoryOnly
                return this
            }

            fun setPriority(priority: Int): Builder {
                this.priority = priority
                return this
            }

            fun setInputData(inputData: ByteArray?): Builder {
                this.inputData = inputData
                return this
            }

            fun build(): Parameters {
                return Parameters(
                    id,
                    createTime,
                    lifespan,
                    maxAttempts,
                    maxInstancesForFactory,
                    maxInstancesForQueue,
                    queue,
                    constraintKeys,
                    inputData,
                    memoryOnly,
                    priority
                )
            }
        }
    }
}