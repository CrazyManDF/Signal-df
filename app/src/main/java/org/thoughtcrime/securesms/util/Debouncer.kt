package org.thoughtcrime.securesms.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.TimeUnit

class Debouncer(private val threshold: Long) {

    private val handler: Handler = Handler(Looper.getMainLooper())

    constructor(threshold: Long, timeUnit: TimeUnit) : this(timeUnit.toMillis(threshold))

    fun publish(runnable: Runnable) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(runnable, threshold)
    }

    fun clear() {
        handler.removeCallbacksAndMessages(null)
    }
}