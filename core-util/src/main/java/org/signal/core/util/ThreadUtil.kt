package org.signal.core.util

import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.annotation.VisibleForTesting
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.Volatile

object ThreadUtil {

    val PRIORITY_BACKGROUND_THREAD: Int = Process.THREAD_PRIORITY_BACKGROUND
    val PRIORITY_IMPORTANT_BACKGROUND_THREAD: Int = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
    val PRIORITY_UI_BLOCKING_THREAD: Int = Process.THREAD_PRIORITY_DEFAULT

    @Volatile
    @VisibleForTesting
    var enforceAssertions: Boolean = true

    val handler: Handler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(Looper.getMainLooper())
    }

    fun isMainThread(): Boolean {
        return Looper.getMainLooper() == Looper.myLooper()
    }

    fun assertMainThread() {
        if (!isMainThread() && enforceAssertions){
            throw AssertionError("Must run on main thread.")
        }
    }

    fun assertNotMainThread() {
        if (isMainThread() && enforceAssertions){
            throw AssertionError("Cannot run on main thread.")
        }
    }

    fun postToMain(runnable: Runnable){
        handler.post(runnable)
    }

    fun runOnMain(runnable: Runnable){
        if (isMainThread()) runnable.run()
        else handler.post(runnable)
    }

    fun runOnMainDelayed(runnable: Runnable, delayMillis: Long) {
        handler.postDelayed(runnable, delayMillis)
    }

    fun cancelRunnableOnMain(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }

    fun runOnMainSync(runnable: Runnable) {
        if (isMainThread()){
            runnable.run()
        } else {
            val sync = CountDownLatch(1)
            runOnMain{
                try {
                    handler.post(runnable)
                }finally {
                    sync.countDown()
                }
            }
            kotlin.runCatching {
                sync.await()
            }.getOrElse {
                throw AssertionError(it)
            }
        }
    }

    fun sleep(millis: Long) {
        kotlin.runCatching {
            Thread.sleep(millis)
        }.getOrElse {
            throw AssertionError(it)
        }
    }

    fun interruptableSleep(millis: Long) {
        kotlin.runCatching {
            Thread.sleep(millis)
        }
    }
}