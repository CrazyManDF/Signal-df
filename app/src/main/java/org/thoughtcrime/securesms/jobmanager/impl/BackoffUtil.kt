package org.thoughtcrime.securesms.jobmanager.impl

import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

object BackoffUtil {

    fun exponentialBackoff(pastAttemptCount: Int, maxBackoff: Long): Long {
        if (pastAttemptCount < 1) {
            throw IllegalArgumentException("Bad attempt count! $pastAttemptCount")
        }

        val boundedAttempt = min(pastAttemptCount, 30)
        val exponentialBackoff = (2.0.pow(boundedAttempt) * 1000).toLong()
        val actualBackoff = min(exponentialBackoff, maxBackoff)
        val jitter = 0.75 + Random.nextDouble(0.0, 1.0) * 0.5
        return (actualBackoff * jitter).toLong()
    }
}