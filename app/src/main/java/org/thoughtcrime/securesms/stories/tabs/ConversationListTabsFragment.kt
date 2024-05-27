package org.thoughtcrime.securesms.stories.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.conversationlist.ConversationListFragment
import org.thoughtcrime.securesms.databinding.ConversationListFragmentBinding
import org.thoughtcrime.securesms.databinding.ConversationListTabsBinding

class ConversationListTabsFragment : Fragment(R.layout.conversation_list_tabs) {

    private val viewModel: ConversationListTabsViewModel by viewModels(ownerProducer = {requireActivity()})

//    private val binding: ConversationListTabsBinding = ConversationListTabsBinding.bind(requireView())


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.chats_tab_touch_point).setOnClickListener {
            viewModel.onChatsSelected()
        }
        view.findViewById<View>(R.id.calls_tab_touch_point).setOnClickListener {
            viewModel.onCallsSelected()
        }
        view.findViewById<View>(R.id.stories_tab_touch_point).setOnClickListener {
            viewModel.onStoriesSelected()
        }
    }
}