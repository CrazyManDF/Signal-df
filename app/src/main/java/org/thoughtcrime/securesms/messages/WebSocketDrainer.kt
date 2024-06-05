package org.thoughtcrime.securesms.messages

import android.os.PowerManager
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Runnable
import org.signal.core.util.Stopwatch
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.JobTracker
import org.thoughtcrime.securesms.jobs.MarkerJob
import org.thoughtcrime.securesms.jobs.PushProcessMessageJob
import org.thoughtcrime.securesms.util.NetworkUtil
import org.thoughtcrime.securesms.util.PowerManagerCompat
import org.thoughtcrime.securesms.util.ServiceUtil
import org.thoughtcrime.securesms.util.WakeLockUtil
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object WebSocketDrainer {

    private val TAG = Log.tag(WebSocketDrainer::class.java)

    private const val KEEP_ALIVE_TOKEN = "WebsocketStrategy"
    private const val WAKELOCK_PREFIX = "websocket-strategy-"

    private val QUEUE_TIMEOUT = 30.seconds.inWholeMilliseconds

    private val NO_NETWORK_WEBSOCKET_TIMEOUT = 5.seconds.inWholeMilliseconds


    @WorkerThread
    fun blockUntilDrainedAndProcessed(): Boolean {
        return blockUntilDrainedAndProcessed(1.minutes.inWholeMilliseconds)
    }

    @WorkerThread
    fun blockUntilDrainedAndProcessed(
        requestedWebsocketDrainTimeoutMs: Long,
        keepAliveToken: String = KEEP_ALIVE_TOKEN
    ): Boolean {
        Log.d(
            TAG,
            "blockUntilDrainedAndProcessed() requestedWebsocketDrainTimeout: $requestedWebsocketDrainTimeoutMs ms"
        )

        var websocketDrainTimeout = requestedWebsocketDrainTimeoutMs

        val context = ApplicationDependencies.getApplication()
        val incomingMessageObserver = ApplicationDependencies.incomingMessageObserver
        val powerManager = ServiceUtil.getPowerManager(context)


        val doze = PowerManagerCompat.isDeviceIdleMode(powerManager)
        val network = NetworkUtil.isConnected(context)

        if (doze || !network) {
            Log.w(
                TAG,
                "We may be operating in a constrained environment. Doze: $doze Network: $network."
            )
        }

        if (!network) {
            Log.w(
                TAG,
                "Network is unavailable. Reducing websocket timeout to $NO_NETWORK_WEBSOCKET_TIMEOUT ms"
            )
            websocketDrainTimeout = NO_NETWORK_WEBSOCKET_TIMEOUT
        }

        val wakeLockTag = WAKELOCK_PREFIX + System.currentTimeMillis()
        val wakeLock = WakeLockUtil.acquire(
            ApplicationDependencies.getApplication(),
            PowerManager.PARTIAL_WAKE_LOCK,
            websocketDrainTimeout + QUEUE_TIMEOUT,
            wakeLockTag
        )

        return try {
            drainAndProcess(websocketDrainTimeout, incomingMessageObserver, keepAliveToken)
        } finally {
            WakeLockUtil.release(wakeLock, wakeLockTag)
        }
    }

    @WorkerThread
    private fun drainAndProcess(
        timeout: Long,
        incomingMessageObserver: IncomingMessageObserver,
        keepAliveToken: String
    ): Boolean {
        val stopwatch = Stopwatch("websocket-strategy")

        val jobManager = ApplicationDependencies.jobManager
        val queueListener = QueueFindingJobListener()

        jobManager.addListener(object : JobTracker.JobFilter {
            override fun matches(job: Job): Boolean {
                return job.parameters.queue?.startsWith(PushProcessMessageJob.QUEUE_PREFIX) ?: false
            }
        }, queueListener)

        val successfullyDrained =
            blockUntilWebsocketDrained(incomingMessageObserver, timeout, keepAliveToken)
        if (!successfullyDrained) {
            return false
        }

        stopwatch.split("decryptions-drained")

        val processQueues = queueListener.getQueues()
        Log.d(TAG, "Discovered " + processQueues.size + " queue(s): " + processQueues)

        for (queue in processQueues) {
            val queueDrained = blockUntilJobQueueDrained(queue, QUEUE_TIMEOUT)
            if (!queueDrained) {
                return false
            }
        }

        stopwatch.split("process-drained")
        stopwatch.stop(TAG)
        return true
    }

    private fun blockUntilWebsocketDrained(
        incomingMessageObserver: IncomingMessageObserver,
        timeoutMs: Long,
        keepAliveToken: String
    ): Boolean {
        try {
            val latch = CountDownLatch(1)
            var success = false

            incomingMessageObserver.registerKeepAliveToken(keepAliveToken) {
                Log.w(TAG, "Keep alive token purged")
                latch.countDown()
            }

            incomingMessageObserver.addDecryptionDrainedListener(object : Runnable {
                override fun run() {
                    success = true
                    latch.countDown()
                    incomingMessageObserver.removeDecryptionDrainedListener(this)
                }
            })

            return try {
                if (!latch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                    Log.w(TAG, "Hit timeout while waiting for decryptions to drain!")
                }
                success
            } catch (e: InterruptedException) {
                Log.w(TAG, "Interrupted!", e)
                false
            }
        } finally {
            incomingMessageObserver.removeKeepAliveToken(keepAliveToken)
        }
    }

    private fun blockUntilJobQueueDrained(queue: String, timeoutMs: Long): Boolean {
        val startTime = System.currentTimeMillis()
        val jobManager = ApplicationDependencies.jobManager
        val markerJob = MarkerJob(queue)
        val jobState = jobManager.runSynchronously(markerJob, timeoutMs)

        if (!jobState.isPresent) {
            Log.w(TAG, "Timed out waiting for $queue job(s) to finish!")
            return false
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        Log.d(TAG, "Waited $duration ms for the $queue job(s) to finish.")
        return true
    }

    private class QueueFindingJobListener : JobTracker.JobListener {

        private val queues: MutableSet<String> = HashSet()


        override fun onStateChanged(job: Job, jobState: JobTracker.JobState) {
            synchronized(queues) {
                job.parameters.queue?.let { queue ->
                    queues += queue
                }
            }
        }

        fun getQueues(): Set<String> {
            synchronized(queues) {
                return queues
            }
        }

    }
}