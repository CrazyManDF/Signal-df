package org.thoughtcrime.securesms.jobmanager

import android.text.TextUtils

object JobLogger {

    fun format(job: Job, event: String): String {
        return format(job, "", event)
    }

    fun format(job: Job, extraTag: String, event: String): String {
        val id = job.getId()
        val tag = if (TextUtils.isEmpty(extraTag)) "" else "[$extraTag]"
        val timeSinceSubmission = System.currentTimeMillis() - job.parameters.createTime
        val runAttempt = job.runAttempt + 1
        val maxAttempts = if (job.parameters.maxAttempts == Job.Parameters.UNLIMITED) "Unlimited" else job.parameters.maxAttempts.toString()
        val lifespan = if (job.parameters.lifespan == Job.Parameters.IMMORTAL) "Immortal" else job.parameters.lifespan.toString() + " ms"

        return "[JOB::$id][${job.javaClass.simpleName}]$tag $event (Time Since Submission: $timeSinceSubmission ms, Lifespan: $lifespan, Run Attempt: $runAttempt/$maxAttempts, Queue: ${job.parameters.queue})"

    }
}