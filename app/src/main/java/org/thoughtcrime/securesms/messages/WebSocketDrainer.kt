package org.thoughtcrime.securesms.messages

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object WebSocketDrainer {

    fun blockUntilDrainedAndProcessed(): Boolean {
        Thread.sleep(10.seconds.inWholeMilliseconds)
        return false
    }
}