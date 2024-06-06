package org.thoughtcrime.securesms.crypto

import org.whispersystems.signalservice.api.SignalSessionLock
import java.io.Closeable
import java.util.concurrent.locks.ReentrantLock

enum class ReentrantSessionLock : SignalSessionLock {

    INSTANCE;

    private val LOCK = ReentrantLock()

    override fun acquire(): SignalSessionLock.Lock {
        LOCK.lock()
        return object : SignalSessionLock.Lock {
            override fun close() {
                LOCK.unlock()
            }
        }
    }

    val isHeldByCurrentThread: Boolean
        get() = LOCK.isHeldByCurrentThread

}
