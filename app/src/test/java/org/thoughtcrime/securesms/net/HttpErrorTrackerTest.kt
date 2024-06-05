package org.thoughtcrime.securesms.net

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class HttpErrorTrackerTest {


    private val START_TIME = TimeUnit.MINUTES.toMillis(60)
    private val SHORT_TIME = TimeUnit.SECONDS.toMillis(1)
    private val LONG_TIME = TimeUnit.SECONDS.toMillis(30)
    private val REALLY_LONG_TIME = TimeUnit.SECONDS.toMillis(90)

    @Test
    fun addSample() {
        val tracker = HttpErrorTracker(2, TimeUnit.MINUTES.toMillis(1))

        // First sample
        Assert.assertFalse(tracker.addSample(START_TIME))
    }
}