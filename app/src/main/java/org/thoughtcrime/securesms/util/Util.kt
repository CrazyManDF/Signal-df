package org.thoughtcrime.securesms.util

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition

object Util {

    fun wait(condition: Condition, timeout: Long) {
        runCatching {
            condition.await(timeout, TimeUnit.MILLISECONDS)
        }.getOrElse { ie ->
            throw AssertionError(ie)
        }
    }
}