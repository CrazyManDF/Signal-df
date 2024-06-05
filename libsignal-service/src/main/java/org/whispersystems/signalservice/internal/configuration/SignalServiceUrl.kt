package org.whispersystems.signalservice.internal.configuration

import okhttp3.ConnectionSpec
import org.whispersystems.signalservice.api.push.TrustStore

class SignalServiceUrl : SignalUrl {

    constructor(
        url: String,
        trustStore: TrustStore,
    ) : super(url, trustStore)

    constructor(
        url: String,
        hostHeader: String?,
        trustStore: TrustStore,
        connectionSpec: ConnectionSpec?
    ) : super(url, hostHeader, trustStore, connectionSpec)
}