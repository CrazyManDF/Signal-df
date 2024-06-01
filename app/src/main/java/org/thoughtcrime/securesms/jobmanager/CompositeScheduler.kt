package org.thoughtcrime.securesms.jobmanager

class CompositeScheduler(private val schedulers: Array<Scheduler>) : Scheduler {
    override fun schedule(delay: Long, constraints: List<Constraint>) {
        schedulers.forEach {scheduler ->
            scheduler.schedule(delay, constraints)
        }
    }
}