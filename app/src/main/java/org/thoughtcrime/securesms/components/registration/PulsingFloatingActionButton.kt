package org.thoughtcrime.securesms.components.registration

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PulsingFloatingActionButton : FloatingActionButton {

    private var pulsing = false
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun startPulse(periodMillis: Long){
        if (!pulsing) {
            pulsing = true
            pulse(periodMillis)
        }
    }

    fun stopPulse() {
        pulsing = false
    }

    private fun pulse(periodMillis: Long) {
        if (!pulsing) return

    }
}