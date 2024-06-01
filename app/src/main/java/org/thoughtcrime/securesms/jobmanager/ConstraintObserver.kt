package org.thoughtcrime.securesms.jobmanager

interface ConstraintObserver {
    fun register(notifier: Notifier)
    interface Notifier {
        fun onConstraintMet(reason: String)
    }
}