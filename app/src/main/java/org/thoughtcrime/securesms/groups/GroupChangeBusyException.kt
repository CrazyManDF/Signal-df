package org.thoughtcrime.securesms.groups

class GroupChangeBusyException : GroupChangeException {

    constructor(throwable: Throwable): super(throwable)
    constructor(message: String): super(message)

}