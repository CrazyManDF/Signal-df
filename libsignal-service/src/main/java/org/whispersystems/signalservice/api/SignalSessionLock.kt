package org.whispersystems.signalservice.api

import java.io.Closeable

interface SignalSessionLock {

    fun acquire(): Lock

    interface Lock : Closeable {
        override fun close()
    }
}