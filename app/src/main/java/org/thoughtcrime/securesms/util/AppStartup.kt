package org.thoughtcrime.securesms.util

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import java.util.LinkedList


object AppStartup {

    private val UI_WAIT_TIME: Long = 500

    private val FAILSAFE_RENDER_TIME: Long = 2500

    private val TAG = Log.tag(AppStartup::class.java)

    private val blocking: LinkedList<Task> = LinkedList<Task>()
    private val nonBlocking: LinkedList<Task> = LinkedList<Task>()
    private val postRender: LinkedList<Task> = LinkedList<Task>()
    private val postRenderHandler: Handler = Handler(Looper.getMainLooper())


    private var applicationStartTime: Long = 0
    private val renderStartTime: Long = 0
    private val renderEndTime: Long = 0

    fun onApplicationCreate() {
        this.applicationStartTime = System.currentTimeMillis()
    }

    @MainThread
    fun addBlocking(name: String, task: Runnable): AppStartup {
        blocking.add(Task(name, task))
        return this
    }

    @MainThread
    fun addNonBlocking(task: Runnable): AppStartup {
        nonBlocking.add(Task("", task))
        return this
    }

    @MainThread
    fun addPostRender(task: Runnable): AppStartup {
        postRender.add(Task("", task))
        return this
    }

    @MainThread
    fun execute() {
        for (task in blocking) {
            task.runnable.run()
        }
        blocking.clear()

        for (task in nonBlocking) {
            SignalExecutors.BOUNDED.execute(task.runnable)
        }
        nonBlocking.clear()

        postRenderHandler.postDelayed(
            {
                executePostRender()
            },
            UI_WAIT_TIME
        )
    }

    private fun executePostRender() {
        for (task in postRender) {
            SignalExecutors.BOUNDED.execute(task.runnable)
        }
        postRender.clear()
    }

//    companion object {
//
//        private val INSTANCE = AppStartup()
//        fun getInstance(): AppStartup {
//            return INSTANCE
//        }
//    }

    private class Task(val name: String, val runnable: Runnable)
}