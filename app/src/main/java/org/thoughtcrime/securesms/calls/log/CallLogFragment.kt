package org.thoughtcrime.securesms.calls.log

import android.os.Bundle
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.ViewBinderDelegate
import org.thoughtcrime.securesms.databinding.CallLogFragmentBinding

class CallLogFragment : Fragment(R.layout.call_log_fragment) {

    companion object {
        private val TAG = Log.tag(CallLogFragment::class.java)
    }

    private val binding: CallLogFragmentBinding by ViewBinderDelegate(CallLogFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSharedElementTransition()
    }

    private fun initializeSharedElementTransition() {
        ViewCompat.setTransitionName(binding.fab, "new_convo_fab")
        ViewCompat.setTransitionName(binding.fabSharedElementTarget, "new_convo_fab")

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