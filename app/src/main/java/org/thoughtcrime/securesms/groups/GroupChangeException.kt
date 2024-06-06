package org.thoughtcrime.securesms.groups

abstract class GroupChangeException : Exception {
    internal constructor()

    internal constructor(throwable: Throwable) : super(throwable)

    internal constructor(message: String) : super(message)
}