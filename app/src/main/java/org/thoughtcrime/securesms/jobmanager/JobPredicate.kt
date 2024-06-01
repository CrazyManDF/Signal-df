package org.thoughtcrime.securesms.jobmanager

import org.thoughtcrime.securesms.jobmanager.persistence.JobSpec


interface JobPredicate {
    companion object {
        val NONE: JobPredicate
            get() = object : JobPredicate {
                override fun shouldRun(jobSpec: JobSpec): Boolean {
                    return true
                }
            }
    }

    fun shouldRun(jobSpec: JobSpec): Boolean
}