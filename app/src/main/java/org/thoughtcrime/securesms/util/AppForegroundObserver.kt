package org.thoughtcrime.securesms.util

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import org.signal.core.util.ThreadUtil
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.Volatile

class AppForegroundObserver {

    private val listeners = CopyOnWriteArraySet<Listener>()

    @Volatile
    private var isForegrounded: Boolean? = null

    @MainThread
    fun begin() {
        ThreadUtil.isMainThread()

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                onForeground()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                onBackground()
            }
        })
    }


    @AnyThread
    fun addListener(listener: Listener) {
        listeners.add(listener)

        isForegrounded?.let {
            if (it) {
                listener.onForeground()
            } else {
                listener.onBackground()
            }
        }
    }

    @AnyThread
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun isForegrounded(): Boolean {
        return isForegrounded == true
    }

    @WorkerThread
    fun onForeground() {
        isForegrounded = true

        for (listener in listeners) {
            listener.onForeground()
        }
    }

    @MainThread
    fun onBackground() {
        isForegrounded = false

        for (listener in listeners) {
            listener.onBackground()
        }
    }

    interface Listener {
        fun onForeground() {}
        fun onBackground() {}
    }
}