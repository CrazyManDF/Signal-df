package org.thoughtcrime.securesms

import android.app.Application
import androidx.annotation.VisibleForTesting
import org.signal.core.util.logging.AndroidLogger
import org.signal.core.util.logging.Log
import org.signal.core.util.logging.Log.tag
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.dependencies.ApplicationDependencyProvider
import org.thoughtcrime.securesms.util.AppStartup

class ApplicationContext : Application() {

    companion object {

        private val TAG = tag(ApplicationContext::class.java)
    }

    override fun onCreate() {
        super.onCreate()

        AppStartup
            .addBlocking("logging") {
                initializeLogging()
                Log.i(TAG, "onCreate()")
            }
            .addBlocking("app-dependencies", this::initializeAppDependencies)
            .addNonBlocking(this::beginJobLoop)
            .execute()
    }

    @VisibleForTesting
    private fun initializeAppDependencies() {
        ApplicationDependencies.init(this, ApplicationDependencyProvider(this))
    }

    @VisibleForTesting
    protected fun initializeLogging() {
        Log.initialize(AndroidLogger())
        AppStartup.onApplicationCreate()
    }

    protected fun beginJobLoop() {
        ApplicationDependencies.jobManager.beginJobLoop()
    }
}