package org.thoughtcrime.securesms

import android.app.Application
import androidx.annotation.VisibleForTesting
import org.signal.core.util.logging.AndroidLogger
import org.signal.core.util.logging.Log

class ApplicationContext : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
    }

    @VisibleForTesting
    protected fun initializeLogging() {
        Log.initialize(AndroidLogger())
    }
}