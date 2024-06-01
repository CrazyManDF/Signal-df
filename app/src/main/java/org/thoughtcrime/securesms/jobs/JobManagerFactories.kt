package org.thoughtcrime.securesms.jobs

import android.app.Application
import org.thoughtcrime.securesms.jobmanager.Constraint
import org.thoughtcrime.securesms.jobmanager.ConstraintObserver
import org.thoughtcrime.securesms.jobmanager.Job

object JobManagerFactories {

    fun <T : Job> getJobFactories(application: Application): Map<String, Job.Factory<T>> = hashMapOf(

    )

    fun <T : Constraint> getConstraintFactories(application: Application): Map<String, Constraint.Factory<T>> = hashMapOf(

    )

    fun getConstraintObservers(application: Application): List<ConstraintObserver> = listOf(

    )

}