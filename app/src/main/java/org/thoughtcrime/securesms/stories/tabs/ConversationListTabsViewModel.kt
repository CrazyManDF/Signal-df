package org.thoughtcrime.securesms.stories.tabs

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ConversationListTabsViewModel : ViewModel() {

    private val behaviorProcessor = BehaviorProcessor.createDefault(ConversationListTabsState())

    val stateFlowable: Flowable<ConversationListTabsState> = behaviorProcessor.onBackpressureLatest()

    val state: Flowable<ConversationListTabsState> = stateFlowable.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread())


    fun onChatsSelected() {
        behaviorProcessor.onNext(
            ConversationListTabsState().copy(tab = ConversationListTab.CHATS)
        )
    }

    fun onCallsSelected() {
        behaviorProcessor.onNext(
            ConversationListTabsState().copy(tab = ConversationListTab.CALLS)
        )
    }

    fun onStoriesSelected() {
        behaviorProcessor.onNext(
            ConversationListTabsState().copy(tab = ConversationListTab.STORIES)
        )
    }
}