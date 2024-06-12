package org.thoughtcrime.securesms.util

import java.util.concurrent.TimeUnit

object FeatureFlags {

    fun getDefaultMaxBackoff(): Long {
        return TimeUnit.SECONDS.toMillis(60)
    }

    fun internalUser(): Boolean {
//        return getBoolean(
//            INTERNAL_USER,
//            false
//        ) || Environment.IS_NIGHTLY || Environment.IS_STAGING
        return true
    }
}