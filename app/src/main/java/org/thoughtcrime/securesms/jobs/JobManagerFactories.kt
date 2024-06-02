package org.thoughtcrime.securesms.jobs

import android.app.Application
import org.thoughtcrime.securesms.jobmanager.Constraint
import org.thoughtcrime.securesms.jobmanager.ConstraintObserver
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraint
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraintObserver

object JobManagerFactories {


    fun getJobFactories(application: Application): Map<String, Job.Factory<Job>> =
        hashMapOf<String, Job.Factory<Job>>().apply {
            put(MessageFetchJob.KEY, MessageFetchJob.Factory())
        }

    fun getConstraintFactories(application: Application): Map<String, Constraint.Factory<Constraint>> =
        hashMapOf<String, Constraint.Factory<Constraint>>().apply {
            put(NetworkConstraint.KEY, NetworkConstraint.Factory(application))
        }

    fun getConstraintObservers(application: Application): List<ConstraintObserver> = listOf(
        NetworkConstraintObserver(application)
    )

}