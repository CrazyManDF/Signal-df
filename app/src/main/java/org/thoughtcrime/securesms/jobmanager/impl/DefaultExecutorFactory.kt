package org.thoughtcrime.securesms.jobmanager.impl

import android.os.Process
import org.signal.core.util.ThreadUtil
import org.thoughtcrime.securesms.jobmanager.ExecutorFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread

class DefaultExecutorFactory : ExecutorFactory {
    override fun newSingleThreadExecutor(name: String): ExecutorService {
        return Executors.newSingleThreadExecutor {r ->
           object : Thread(r, name) {

               override fun run() {
                   Process.setThreadPriority(ThreadUtil.PRIORITY_BACKGROUND_THREAD)
                   super.run()
               }
           }
        }
    }
}