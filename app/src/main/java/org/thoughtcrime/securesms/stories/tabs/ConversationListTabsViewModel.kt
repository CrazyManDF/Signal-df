package org.thoughtcrime.securesms.stories.tabs

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.thoughtcrime.securesms.util.rx.RxStore

class ConversationListTabsViewModel : ViewModel() {
    private val store = RxStore(ConversationListTabsState())

    val state: Flowable<ConversationListTabsState> = store.stateFlow.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread())


    fun onChatsSelected() {
        performStoreUpdate{
            it.copy(tab = ConversationListTab.CHATS)
        }
    }

    fun onCallsSelected() {
        performStoreUpdate{
            it.copy(tab = ConversationListTab.CALLS)
        }
    }

    fun onStoriesSelected() {
        performStoreUpdate{
            it.copy(tab = ConversationListTab.STORIES)
        }
    }

    private fun performStoreUpdate(fn: (ConversationListTabsState) -> ConversationListTabsState){
        store.update {
            fn(it.copy(prevTab = it.tab))
        }
    }
}