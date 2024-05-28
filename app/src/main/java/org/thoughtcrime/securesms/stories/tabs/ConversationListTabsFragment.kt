package org.thoughtcrime.securesms.stories.tabs

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationSet
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieAnimationView
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.DimensionUnit
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.ViewBinderDelegate
import org.thoughtcrime.securesms.conversationlist.ConversationListFragment
import org.thoughtcrime.securesms.databinding.ConversationListFragmentBinding
import org.thoughtcrime.securesms.databinding.ConversationListTabsBinding
import org.thoughtcrime.securesms.stories.Stories
import org.thoughtcrime.securesms.util.visible

class ConversationListTabsFragment : Fragment(R.layout.conversation_list_tabs) {

    private val viewModel: ConversationListTabsViewModel by viewModels(ownerProducer = {requireActivity()})

    private val binding: ConversationListTabsBinding by ViewBinderDelegate(ConversationListTabsBinding::bind)
    private var shouldBeImmediate = true
    private var pillAnimator: Animator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chatsTabTouchPoint.setOnClickListener {
            viewModel.onChatsSelected()
        }
        binding.callsTabTouchPoint.setOnClickListener {
            viewModel.onCallsSelected()
        }
        binding.storiesTabTouchPoint.setOnClickListener {
            viewModel.onStoriesSelected()
        }

        viewModel.state.subscribeBy {
            update(it, shouldBeImmediate)
            shouldBeImmediate = false
        }
    }

    private fun update(state: ConversationListTabsState, immediate: Boolean) {
        binding.chatsTabIcon.isSelected = state.tab == ConversationListTab.CHATS
        binding.chatsPill.isSelected = state.tab == ConversationListTab.CHATS

        if (Stories.isFeatureEnabled()) {
            binding.storiesTabIcon.isSelected = state.tab == ConversationListTab.STORIES
            binding.storiesPill.isSelected = state.tab == ConversationListTab.STORIES
        }

        binding.callsTabIcon.isSelected = state.tab == ConversationListTab.CALLS
        binding.callsPill.isSelected = state.tab == ConversationListTab.CALLS

        val hasStateChange = state.tab != state.prevTab
        if (immediate) {
            binding.chatsTabIcon.pauseAnimation()
            binding.chatsTabIcon.progress = if (state.tab == ConversationListTab.CHATS) 1f else 0f

            if (Stories.isFeatureEnabled()) {
                binding.storiesTabIcon.pauseAnimation()
                binding.storiesTabIcon.progress = if (state.tab == ConversationListTab.STORIES) 1f else 0f
            }

            binding.callsTabIcon.pauseAnimation()
            binding.callsTabIcon.progress = if (state.tab == ConversationListTab.CALLS) 1f else 0f

            runPillAnimation(
                0,
                listOfNotNull(
                    binding.chatsPill,
                    binding.callsPill,
                    binding.storiesPill.takeIf { Stories.isFeatureEnabled() }
                )
            )
        } else if (hasStateChange) {
            runLottieAnimations(
                listOfNotNull(
                    binding.chatsTabIcon,
                    binding.callsTabIcon,
                    binding.storiesTabIcon.takeIf { Stories.isFeatureEnabled() }
                )
            )

            runPillAnimation(
                150,
                listOfNotNull(
                    binding.chatsPill,
                    binding.callsPill,
                    binding.storiesPill.takeIf { Stories.isFeatureEnabled() }
                )
            )
        }

        binding.chatsUnreadIndicator.visible = state.unreadMessagesCount > 0
        binding.chatsUnreadIndicator.text = formatCount(state.unreadMessagesCount)

        if (Stories.isFeatureEnabled()) {
            binding.storiesUnreadIndicator.visible = state.unreadStoriesCount > 0 || state.hasFailedStory
            binding.storiesUnreadIndicator.text = if (state.hasFailedStory) "!" else formatCount(state.unreadStoriesCount)
        }

        binding.callsUnreadIndicator.visible = state.unreadCallsCount > 0
        binding.callsUnreadIndicator.text = formatCount(state.unreadCallsCount)

        requireView().visible = state.visibilityState.isVisible()
    }

    private fun runLottieAnimations(toAnimate: List<LottieAnimationView>) {
        toAnimate.forEach {
            if (it.isSelected){
                it.resumeAnimation()
            } else {
                if (it.isAnimating){
                    it.pauseAnimation()
                }

                it.progress = 0f
            }
        }
    }

    private fun runPillAnimation(duration: Long, toAnimate: List<ImageView>) {
        val (selected, unselected) =  toAnimate.partition { it.isSelected }

        pillAnimator?.cancel()
        pillAnimator = AnimatorSet().apply {
            this.duration = duration
            playTogether(
                selected.map { view ->
                    view.visibility = View.VISIBLE
                    ValueAnimator.ofInt(view.paddingStart, 0).apply {
                        addUpdateListener {
                            view.setPadding(it.animatedValue as Int, 0, it.animatedValue as Int, 0)
                        }
                    }
                }
            )
            start()
        }

        unselected.forEach {
            val smallPad = DimensionUnit.DP.toPixels(16f).toInt()
            it.setPadding(smallPad, 0, smallPad, 0)
            it.visibility = View.INVISIBLE
        }
    }

    private fun formatCount(count: Long): String {
        if (count > 99L) {
            return getString(R.string.ConversationListTabs__99p)
        }
        return count.toString()
    }
}