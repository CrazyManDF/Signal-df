package org.thoughtcrime.securesms.dependencies

import android.app.Application
import androidx.annotation.VisibleForTesting
import org.thoughtcrime.securesms.database.DatabaseObserver
import org.thoughtcrime.securesms.jobmanager.JobManager
import org.thoughtcrime.securesms.messages.IncomingMessageObserver
import org.thoughtcrime.securesms.push.SignalServiceNetworkAccess
import org.thoughtcrime.securesms.util.AppForegroundObserver
import org.whispersystems.signalservice.api.SignalWebSocket
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration
import org.whispersystems.signalservice.internal.websocket.LibSignalNetwork
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Supplier
import kotlin.concurrent.withLock

object ApplicationDependencies {

    private val LOCK: ReentrantLock = ReentrantLock()
    private val FRAME_RATE_TRACKER_LOCK: ReentrantLock = ReentrantLock()
    private val JOB_MANAGER_LOCK: ReentrantLock = ReentrantLock()
    private val SIGNAL_HTTP_CLIENT_LOCK: ReentrantLock = ReentrantLock()
    private val LIBSIGNAL_NETWORK_LOCK: ReentrantLock = ReentrantLock()

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

    val incomingMessageObserver: IncomingMessageObserver by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        provider!!.provideIncomingMessageObserver()
    }


    val signalWebSocket: SignalWebSocket by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        provider!!.provideSignalWebSocket(
            {
                getSignalServiceNetworkAccess().getConfiguration()!!
            },
            ApplicationDependencies::libsignalNetwork
        )
    }

    val libsignalNetwork: LibSignalNetwork by lazy(LIBSIGNAL_NETWORK_LOCK) {
        provider!!.provideLibsignalNetwork(getSignalServiceNetworkAccess().getConfiguration()!!)
    }

    val databaseObserver: DatabaseObserver  by lazy(LOCK) {
        provider!!.provideDatabaseObserver()
    }

    fun getSignalServiceNetworkAccess(): SignalServiceNetworkAccess {
        return provider!!.provideSignalServiceNetworkAccess()
    }

    interface Provider {

        fun provideJobManager(): JobManager

        fun provideAppForegroundObserver(): AppForegroundObserver

        fun provideIncomingMessageObserver(): IncomingMessageObserver

        fun provideSignalServiceNetworkAccess(): SignalServiceNetworkAccess

        fun provideSignalWebSocket(
            signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>,
            libSignalNetworkSupplier: Supplier<LibSignalNetwork>
        ): SignalWebSocket

        fun provideLibsignalNetwork(config: SignalServiceConfiguration): LibSignalNetwork
        fun provideDatabaseObserver(): DatabaseObserver

    }

}