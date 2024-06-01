package org.thoughtcrime.securesms.util

import java.util.concurrent.TimeUnit

object FeatureFlags {

    fun getDefaultMaxBackoff(): Long {
        return TimeUnit.SECONDS.toMillis(60)
    }
}