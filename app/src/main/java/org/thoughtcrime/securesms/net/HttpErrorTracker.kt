package org.thoughtcrime.securesms.net

import java.util.Arrays

class HttpErrorTracker(
    private val samples: Int,
    private val errorTimeRange: Long
) {

    private val timestamps = Array(samples){0L}

    @Synchronized
    fun addSample(now: Long): Boolean {
        val errorsMustBeAfter = now - errorTimeRange
        var count = 1
        var minIndex = 0

        for (i in timestamps.indices){
            if (timestamps[i] < errorsMustBeAfter){
                timestamps[i] = 0L
            } else if (timestamps[i] != 0L) {
                count++
            }

            if (timestamps[i] < timestamps[minIndex]) {
                minIndex = i
            }
        }

        timestamps[minIndex] = now
        if (count >= timestamps.size) {
            Arrays.fill(timestamps, 0)
            return true
        }
        return false
    }
}