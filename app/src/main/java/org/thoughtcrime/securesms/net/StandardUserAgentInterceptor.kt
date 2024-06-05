package org.thoughtcrime.securesms.net

import android.os.Build
import org.thoughtcrime.securesms.BuildConfig

class StandardUserAgentInterceptor : UserAgentInterceptor(USER_AGENT) {

    companion object {

        val USER_AGENT: String =
            "Signal-Android/" + BuildConfig.VERSION_NAME + " Android/" + Build.VERSION.SDK_INT
    }
}