package org.thoughtcrime.securesms.conversationlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.ViewBinderDelegate
import org.thoughtcrime.securesms.databinding.ConversationListFragmentBinding

class ConversationListFragment : Fragment() {

    private val binding: ConversationListFragmentBinding by ViewBinderDelegate(ConversationListFragmentBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.conversation_list_fragment, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.visibility = View.VISIBLE
        binding.cameraFab.visibility = View.VISIBLE

        binding.fab.show()
        binding.cameraFab.show()
    }
}