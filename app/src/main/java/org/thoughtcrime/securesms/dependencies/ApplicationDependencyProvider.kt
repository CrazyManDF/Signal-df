package org.thoughtcrime.securesms.dependencies

import android.app.Application
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
import org.thoughtcrime.securesms.util.AppForegroundObserver

class ApplicationDependencyProvider(private val context: Application) : ApplicationDependencies.Provider {
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
}