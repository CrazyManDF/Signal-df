package org.thoughtcrime.securesms.util.rx

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabsState

class RxStore<T : Any>(
    defaultValue: T,
    scheduler: Scheduler = Schedulers.computation()
) : Disposable {

    private val behaviorProcessor = BehaviorProcessor.createDefault(defaultValue)
    val stateFlow = behaviorProcessor.onBackpressureLatest()

    private val actionSubject = PublishSubject.create<(T) -> T>()

    private val actionDisposable: Disposable = actionSubject
        .observeOn(scheduler)
        .scan(defaultValue) { v, f -> f(v) }
        .subscribe {
            val s = it as ConversationListTabsState
            behaviorProcessor.onNext(it)
        }

    fun update(transformer: (T) -> T) {
        actionSubject.onNext(transformer)
    }

    override fun dispose() {


    }

    override fun isDisposed(): Boolean {
        return true
    }

}