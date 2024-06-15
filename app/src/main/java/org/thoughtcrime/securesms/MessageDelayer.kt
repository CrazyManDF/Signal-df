package org.thoughtcrime.securesms

import android.os.Handler
import android.os.Looper
import org.thoughtcrime.securesms.IdlingResource.SimpleIdlingResource

object MessageDelayer {

    private const val DELAY_MILLIS = 10*1000L

    interface DelayerCallback {
        fun onDone(text: String)
    }

    fun processMessage(
        message: String,
        callback: DelayerCallback?,
        idlingResource: SimpleIdlingResource
    ) {
        // The IdlingResource is null in production.
        idlingResource.increment()

        // Delay the execution, return message via callback.
        val handler = Handler(Looper.myLooper()!!)
        handler.postDelayed({
            if (callback != null) {
                callback.onDone(message)
                idlingResource.decrement()
            }
        }, DELAY_MILLIS)
    }
}