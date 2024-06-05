package org.whispersystems.signalservice.api.util


/**
 * Convenient ways to assert expected state.
 */
object Preconditions {
    fun checkArgument(state: Boolean, message: String = "Condition must be true!") {
        require(state) { message }
    }

    fun checkState(state: Boolean, message: String = "Condition must be true!") {
        check(state) { message }
    }

    fun <E> checkNotNull(`object`: E): E {
        return checkNotNull<E>(`object`, "Must not be null!")
    }

    fun <E> checkNotNull(`object`: E?, message: String?): E {
        if (`object` == null) {
            throw NullPointerException(message)
        } else {
            return `object`
        }
    }
}
