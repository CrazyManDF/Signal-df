package org.thoughtcrime.securesms.stories.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.ViewBinderDelegate
import org.thoughtcrime.securesms.conversationlist.ConversationListFragment
import org.thoughtcrime.securesms.databinding.ConversationListFragmentBinding
import org.thoughtcrime.securesms.databinding.ConversationListTabsBinding

class ConversationListTabsFragment : Fragment(R.layout.conversation_list_tabs) {

    private val viewModel: ConversationListTabsViewModel by viewModels(ownerProducer = {requireActivity()})

    private val binding: ConversationListTabsBinding by ViewBinderDelegate(ConversationListTabsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(">>>", "onViewCreated===========")

        binding.chatsTabTouchPoint.setOnClickListener {
            viewModel.onChatsSelected()
        }
        binding.callsTabTouchPoint.setOnClickListener {
            viewModel.onCallsSelected()
        }
        binding.storiesTabTouchPoint.setOnClickListener {
            viewModel.onStoriesSelected()
        }
    }
}