package org.thoughtcrime.securesms.dependencies

import android.app.Application
import androidx.annotation.VisibleForTesting
import org.thoughtcrime.securesms.jobmanager.Constraint
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.JobManager
import org.thoughtcrime.securesms.util.AppForegroundObserver
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object ApplicationDependencies {

    private val LOCK: ReentrantLock = ReentrantLock()
    private val JOB_MANAGER_LOCK: ReentrantLock = ReentrantLock()

    private var application: Application? = null

    private var provider: Provider? = null

    var appForegroundObserver: AppForegroundObserver? = null
        private set

    fun init(application: Application, provider: Provider) {
        LOCK.withLock {
            if (this.application != null || this.provider != null) {
                throw IllegalArgumentException("Already initialized!")
            }
            this.application = application
            this.provider = provider
            this.appForegroundObserver = provider.provideAppForegroundObserver().apply {
                begin()
            }
        }
    }

    @VisibleForTesting
    fun isInitialized(): Boolean {
        return application != null
    }

    fun getApplication(): Application {
        return application!!
    }

    val jobManager by lazy(lock = JOB_MANAGER_LOCK) {
        provider!!.provideJobManager()
    }

    interface Provider {

        fun provideJobManager(): JobManager

        fun provideAppForegroundObserver(): AppForegroundObserver
    }

}