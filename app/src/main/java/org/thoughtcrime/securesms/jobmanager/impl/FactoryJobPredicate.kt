package org.thoughtcrime.securesms.jobmanager.impl

import org.thoughtcrime.securesms.jobmanager.JobPredicate
import org.thoughtcrime.securesms.jobmanager.persistence.JobSpec

class FactoryJobPredicate(factories: Array<String>) : JobPredicate {

    private val factories = hashSetOf(*factories)
    override fun shouldRun(jobSpec: JobSpec): Boolean {
        return factories.contains(jobSpec.factoryKey)
    }
}