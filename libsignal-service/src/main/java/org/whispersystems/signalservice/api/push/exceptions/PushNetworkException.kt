package org.whispersystems.signalservice.api.push.exceptions

import java.io.IOException
import java.lang.Exception

class PushNetworkException : IOException {

    constructor(exception: Exception): super(exception)

    constructor(s: String): super(s)

}