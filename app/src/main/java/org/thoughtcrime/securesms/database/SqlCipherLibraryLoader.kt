package org.thoughtcrime.securesms.database

import java.util.concurrent.locks.ReentrantLock

object SqlCipherLibraryLoader {

    @Volatile
    private var loaded = false
    private val LOCK = Any()

    fun load() {
        if (!loaded) {
            synchronized(LOCK) {
                if (!loaded) {
                    System.loadLibrary("sqlcipher")
                    loaded = true
                }
            }
        }
    }
}