package org.thoughtcrime.securesms.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.stories.tabs.ConversationListTab
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabsState
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabsViewModel

class MainActivityListHostFragment : Fragment(R.layout.main_activity_list_host_fragment) {
    companion object {
        private val TAG = Log.tag(MainActivityListHostFragment::class.java)
    }

    private val conversationListTabsViewModel: ConversationListTabsViewModel by viewModels(ownerProducer = {requireActivity()})

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val disposable = conversationListTabsViewModel.state.subscribeBy { state ->
            val controller = requireView().findViewById<View>(R.id.fragment_container).findNavController()
            when (controller.currentDestination?.id) {
                R.id.conversationListFragment -> goToStateFromConversationList(state, controller)
                R.id.storiesLandingFragment -> goToStateFromStories(state, controller)
                R.id.callLogFragment -> goToStateFromCalling(state, controller)
            }
        }
    }

    private fun goToStateFromConversationList(
        state: ConversationListTabsState,
        navController: NavController
    ) {
        if (state.tab == ConversationListTab.CHATS) {
            return
        } else {
            val destination = if (state.tab == ConversationListTab.STORIES) {
                R.id.action_conversationListFragment_to_storiesLandingFragment
            } else {
                R.id.action_conversationListFragment_to_callLogFragment
            }
            navController.navigate(destination)
        }
    }

    private fun goToStateFromCalling(
        state: ConversationListTabsState,
        navController: NavController
    ) {
        when (state.tab) {
            ConversationListTab.CALLS -> return
            ConversationListTab.CHATS -> navController.popBackStack(
                R.id.conversationListFragment,
                false
            )

            ConversationListTab.STORIES -> navController.navigate(R.id.action_callLogFragment_to_storiesLandingFragment)
        }
    }

    private fun goToStateFromStories(
        state: ConversationListTabsState,
        navController: NavController
    ) {
        when (state.tab) {
            ConversationListTab.STORIES -> return
            ConversationListTab.CHATS -> navController.popBackStack(
                R.id.conversationListFragment,
                false
            )

            ConversationListTab.CALLS -> navController.navigate(R.id.action_storiesLandingFragment_to_callLogFragment)
        }
    }

}