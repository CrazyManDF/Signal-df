package org.thoughtcrime.securesms.jobmanager

import java.util.concurrent.ExecutorService

interface ExecutorFactory {
    fun newSingleThreadExecutor(name: String): ExecutorService
}