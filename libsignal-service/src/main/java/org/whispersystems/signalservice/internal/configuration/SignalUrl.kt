package org.whispersystems.signalservice.internal.configuration

import okhttp3.ConnectionSpec
import org.whispersystems.signalservice.api.push.TrustStore
import java.util.Optional

open class SignalUrl(
    val url: String,
    hostHeader: String?,
    val trustStore: TrustStore,
    connectionSpec: ConnectionSpec?
) {

    val hostHeader = Optional.ofNullable(hostHeader)
    val connectionSpec = Optional.ofNullable(connectionSpec)

    constructor(url: String, trustStore: TrustStore) : this(url, null, trustStore, null)

    fun getConnectionSpecs(): Optional<List<ConnectionSpec>> {
        return if (connectionSpec.isPresent) Optional.of(listOf(connectionSpec.get())) else Optional.empty()
    }
}