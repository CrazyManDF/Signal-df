package org.whispersystems.signalservice.api.util

interface SleepTimer {
    @Throws(InterruptedException::class)
    fun sleep(millis: Long)
}