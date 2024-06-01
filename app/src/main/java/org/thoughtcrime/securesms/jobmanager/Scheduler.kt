package org.thoughtcrime.securesms.jobmanager

interface Scheduler {
    fun schedule(delay: Long, constraints: List<Constraint>)
}