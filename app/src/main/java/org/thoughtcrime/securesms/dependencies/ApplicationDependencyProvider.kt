package org.thoughtcrime.securesms.dependencies

import android.app.Application
import org.signal.libsignal.net.Network
import org.thoughtcrime.securesms.BuildConfig
import org.thoughtcrime.securesms.database.DatabaseObserver
import org.thoughtcrime.securesms.database.JobDatabase
import org.thoughtcrime.securesms.jobmanager.Constraint
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.JobManager
import org.thoughtcrime.securesms.jobmanager.impl.FactoryJobPredicate
import org.thoughtcrime.securesms.jobs.FastJobStorage
import org.thoughtcrime.securesms.jobs.GroupCallUpdateSendJob
import org.thoughtcrime.securesms.jobs.IndividualSendJob
import org.thoughtcrime.securesms.jobs.JobManagerFactories
import org.thoughtcrime.securesms.jobs.MarkerJob
import org.thoughtcrime.securesms.jobs.PushGroupSendJob
import org.thoughtcrime.securesms.jobs.PushProcessMessageJob
import org.thoughtcrime.securesms.jobs.ReactionSendJob
import org.thoughtcrime.securesms.jobs.TypingSendJob
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.messages.IncomingMessageObserver
import org.thoughtcrime.securesms.net.DefaultWebSocketShadowingBridge
import org.thoughtcrime.securesms.net.SignalWebSocketHealthMonitor
import org.thoughtcrime.securesms.net.StandardUserAgentInterceptor
import org.thoughtcrime.securesms.push.SignalServiceNetworkAccess
import org.thoughtcrime.securesms.stories.Stories
import org.thoughtcrime.securesms.util.AlarmSleepTimer
import org.thoughtcrime.securesms.util.AppForegroundObserver
import org.whispersystems.signalservice.api.SignalWebSocket
import org.whispersystems.signalservice.api.push.ServiceId
import org.whispersystems.signalservice.api.util.CredentialsProvider
import org.whispersystems.signalservice.api.util.UptimeSleepTimer
import org.whispersystems.signalservice.api.websocket.WebSocketFactory
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration
import org.whispersystems.signalservice.internal.websocket.LibSignalNetwork
import org.whispersystems.signalservice.internal.websocket.OkHttpWebSocketConnection
import org.whispersystems.signalservice.internal.websocket.WebSocketConnection
import org.whispersystems.signalservice.internal.websocket.WebSocketShadowingBridge
import java.util.Optional
import java.util.function.Supplier

class ApplicationDependencyProvider(private val context: Application) :
    ApplicationDependencies.Provider {
    override fun provideJobManager(): JobManager {
        val config = JobManager.Configuration.Builder<Job, Constraint>()
            .setJobFactories(JobManagerFactories.getJobFactories(context))
            .setConstraintFactories(JobManagerFactories.getConstraintFactories(context))
            .setConstraintObservers(JobManagerFactories.getConstraintObservers(context))
            .setJobStorage(FastJobStorage(JobDatabase.getInstance(context)))
//            .setJobMigrator(
//                JobMigrator(
//                    TextSecurePreferences.getJobManagerVersion(context),
//                    JobManager.CURRENT_VERSION,
//                    JobManagerFactories.getJobMigrations(context)
//                )
//            )
            .addReservedJobRunner(
                FactoryJobPredicate(
                    arrayOf(
                        PushProcessMessageJob.KEY,
                        MarkerJob.KEY
                    )
                )
            )
            .addReservedJobRunner(
                FactoryJobPredicate(
                    arrayOf(
                        IndividualSendJob.KEY,
                        PushGroupSendJob.KEY,
                        ReactionSendJob.KEY,
                        TypingSendJob.KEY,
                        GroupCallUpdateSendJob.KEY
                    )
                )
            )
            .build()
        return JobManager(context, config)
    }

    override fun provideAppForegroundObserver(): AppForegroundObserver {
        return AppForegroundObserver()
    }

    override fun provideIncomingMessageObserver(): IncomingMessageObserver {
        return IncomingMessageObserver(context)
    }

    override fun provideSignalServiceNetworkAccess(): SignalServiceNetworkAccess {
        return SignalServiceNetworkAccess(context)
    }


    override fun provideSignalWebSocket(
        signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>,
        libSignalNetworkSupplier: Supplier<LibSignalNetwork>
    ): SignalWebSocket {
        val sleepTimer =
            if (!SignalStore.account().fcmEnabled || SignalStore.internalValues().isWebsocketModeForced)
                AlarmSleepTimer(context) else UptimeSleepTimer()

        val healthMonitor = SignalWebSocketHealthMonitor(context, sleepTimer)
        val bridge = DefaultWebSocketShadowingBridge(context)
        val signalWebSocket = SignalWebSocket(
            provideWebSocketFactory(
                signalServiceConfigurationSupplier,
                healthMonitor,
                libSignalNetworkSupplier,
                bridge
            )
        )
        healthMonitor.monitor(signalWebSocket)

        return signalWebSocket
    }


    fun provideWebSocketFactory(
        signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>,
        healthMonitor: SignalWebSocketHealthMonitor,
        libSignalNetworkSupplier: Supplier<LibSignalNetwork>,
        bridge: WebSocketShadowingBridge
    ): WebSocketFactory {
        return object : WebSocketFactory {

            override fun createWebSocket(): WebSocketConnection {
                return OkHttpWebSocketConnection(
                    "normal",
                    signalServiceConfigurationSupplier.get(),
                    Optional.of(DynamicCredentialsProvider()),
                    BuildConfig.SIGNAL_AGENT,
                    healthMonitor,
                    Stories.isFeatureEnabled()
                )
            }

            override fun createUnidentifiedWebSocket(): WebSocketConnection {
                return OkHttpWebSocketConnection(
                    "normal",
                    signalServiceConfigurationSupplier.get(),
                    Optional.of(DynamicCredentialsProvider()),
                    BuildConfig.SIGNAL_AGENT,
                    healthMonitor,
                    Stories.isFeatureEnabled()
                )
            }
        }
    }

    override fun provideLibsignalNetwork(config: SignalServiceConfiguration): LibSignalNetwork {
        return LibSignalNetwork(
            Network(BuildConfig.LIBSIGNAL_NET_ENV, StandardUserAgentInterceptor.USER_AGENT),
            config
        )
    }

    override fun provideDatabaseObserver(): DatabaseObserver {
        return DatabaseObserver(context)
    }

    class DynamicCredentialsProvider : CredentialsProvider {
        override fun getAci(): ServiceId.ACI? {
            return SignalStore.account().aci
        }

        override fun getPni(): ServiceId.PNI? {
            return SignalStore.account().pni
        }

        override fun getE164(): String? {
            return SignalStore.account().e164
        }

        override fun getDeviceId(): Int {
            return SignalStore.account().deviceId
        }

        override fun getPassword(): String? {
            return SignalStore.account().servicePassword
        }

    }

}