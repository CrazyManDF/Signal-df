package org.whispersystems.signalservice.api.util

class UptimeSleepTimer : SleepTimer {
    @Throws(InterruptedException::class)
    override fun sleep(millis: Long) {
        Thread.sleep(millis)
    }
}