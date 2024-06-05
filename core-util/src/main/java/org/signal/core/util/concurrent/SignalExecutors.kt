package org.signal.core.util.concurrent

import android.os.HandlerThread
import android.os.Process
import org.signal.core.util.ThreadUtil
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object SignalExecutors {

    val UNBOUNDED: ExecutorService = Executors.newCachedThreadPool(
        NumberedThreadFactory(
            "signal-unbounded",
            ThreadUtil.PRIORITY_BACKGROUND_THREAD
        )
    )
    val BOUNDED: ExecutorService = Executors.newFixedThreadPool(
        4,
        NumberedThreadFactory("signal-bounded", ThreadUtil.PRIORITY_BACKGROUND_THREAD)
    )
    val SERIAL: ExecutorService = Executors.newSingleThreadExecutor(
        NumberedThreadFactory(
            "signal-serial",
            ThreadUtil.PRIORITY_BACKGROUND_THREAD
        )
    )
    val BOUNDED_IO: ExecutorService = newCachedBoundedExecutor(
        "signal-io-bounded",
        ThreadUtil.PRIORITY_IMPORTANT_BACKGROUND_THREAD,
        1,
        32,
        30
    );

    @JvmStatic
    fun newCachedSingleThreadExecutor(name: String, priority: Int): ExecutorService {
        val executor = ThreadPoolExecutor(1, 1, 15, TimeUnit.SECONDS, LinkedBlockingQueue(),
            ThreadFactory {
                object : Thread(it, name) {
                    override fun run() {
                        Process.setThreadPriority(priority)
                        super.run()
                    }
                }
            })
        executor.allowsCoreThreadTimeOut()
        return executor
    }


    fun newCachedBoundedExecutor(
        name: String,
        priority: Int,
        minThreads: Int,
        maxThreads: Int,
        timeoutSeconds: Long
    ): ExecutorService {

        val threadPool = ThreadPoolExecutor(
            minThreads,
            maxThreads,
            timeoutSeconds,
            TimeUnit.SECONDS,
            object : LinkedBlockingQueue<Runnable>() {
                override fun offer(e: Runnable?): Boolean {
                    return if (isEmpty()) {
                        super.offer(e)
                    } else {
                        false
                    }
                }
            },
            NumberedThreadFactory(name, priority)
        )
        threadPool.setRejectedExecutionHandler { r, executor ->
            runCatching {
                executor.queue.put(r)
            }.onFailure {
                Thread.currentThread().interrupt()
            }
        }
        return threadPool
    }

    fun newFixedLifoThreadExecutor(
        name: String,
        minThreads: Int,
        maxThreads: Int
    ): ExecutorService {
        return ThreadPoolExecutor(
            minThreads,
            maxThreads,
            0,
            TimeUnit.MILLISECONDS,
            LinkedBlockingDeque(),
            NumberedThreadFactory(
                name, ThreadUtil.PRIORITY_BACKGROUND_THREAD
            )
        )
    }

    fun getAndStartHandlerThread(name: String, priority: Int): HandlerThread {
        val handlerThread = HandlerThread(name, priority)
        handlerThread.start()
        return handlerThread
    }

    private class NumberedThreadFactory(
        private val baseName: String,
        private val priorityNew: Int
    ) : ThreadFactory {

        private val counter = AtomicInteger()

        override fun newThread(r: Runnable): Thread {
            return object : Thread(r, "$baseName-${counter.getAndIncrement()}") {
                override fun run() {
                    Process.setThreadPriority(priorityNew)
                    super.run()
                }
            }
        }

    }
}