package org.thoughtcrime.securesms.util

import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.jobmanager.Job
import org.thoughtcrime.securesms.jobmanager.JobManager

fun Job.asChain(): JobManager.Chain {
    return ApplicationDependencies.jobManager.startChain(this)
}