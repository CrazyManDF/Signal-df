package org.thoughtcrime.securesms.stories.landing

import android.os.Bundle
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.calls.log.CallLogFragment
import org.thoughtcrime.securesms.components.ViewBinderDelegate
import org.thoughtcrime.securesms.databinding.CallLogFragmentBinding
import org.thoughtcrime.securesms.databinding.StoriesLandingFragmentBinding

class StoriesLandingFragment : Fragment(R.layout.stories_landing_fragment) {

    companion object {
        private val TAG = Log.tag(StoriesLandingFragment::class.java)
    }

    private val binding: StoriesLandingFragmentBinding by ViewBinderDelegate(StoriesLandingFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setTransitionName(binding.cameraFab, "new_convo_fab")
        ViewCompat.setTransitionName(binding.cameraFabSharedElementTarget, "camera_fab")

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementStart(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
            ) {
                sharedElementNames?.forEach {
                    Log.d(TAG, "====$it")
                }
            }
        })
    }
}