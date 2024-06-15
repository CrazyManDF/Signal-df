package org.thoughtcrime.securesms.IdlingResource

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import java.util.concurrent.atomic.AtomicInteger


class  SimpleIdlingResource : IdlingResource {

    private val counter = AtomicInteger(0)

    @Volatile
    private var resourceCallback: ResourceCallback? = null
    override fun getName(): String {
        return this.javaClass.name
    }

    override fun registerIdleTransitionCallback(callback: ResourceCallback?) {
        this.resourceCallback = callback
    }

    override fun isIdleNow(): Boolean {
        return counter.get() == 0
    }

    fun increment() {
        counter.getAndIncrement()
    }

    fun decrement() {
        val counterVal = counter.decrementAndGet()
        if (counterVal == 0) {
            // 执行onTransitionToIdle()方法，告诉Espresso，当前是空闲状态。
            resourceCallback?.onTransitionToIdle()
        }

        require(counterVal >= 0) { "Counter has been corrupted!" }
    }

}