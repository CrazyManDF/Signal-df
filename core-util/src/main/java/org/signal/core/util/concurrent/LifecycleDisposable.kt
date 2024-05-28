package org.signal.core.util.concurrent

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

class LifecycleDisposable : DefaultLifecycleObserver {
    val disposables: CompositeDisposable = CompositeDisposable()
    fun bindTo(lifecycleOwner: LifecycleOwner) {
        bindTo(lifecycleOwner.lifecycle)
    }

    fun bindTo(lifecycle: Lifecycle): LifecycleDisposable {
        lifecycle.addObserver(this)
        return this
    }

    fun add(disposable: Disposable): LifecycleDisposable {
        disposables.add(disposable)
        return this
    }

    fun addAll(vararg disposable: Disposable): LifecycleDisposable {
        disposables.addAll(*disposable)
        return this
    }

    fun clear() {
        disposables.clear()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
        disposables.clear()
    }

    operator fun plusAssign(disposable: Disposable) {
        add(disposable)
    }

    fun Disposable.addTo(lifecycleDisposable: LifecycleDisposable): Disposable = apply {
        lifecycleDisposable.add(this)
    }
}