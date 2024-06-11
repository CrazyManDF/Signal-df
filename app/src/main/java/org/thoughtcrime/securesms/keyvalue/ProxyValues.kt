package org.thoughtcrime.securesms.keyvalue

import org.whispersystems.signalservice.internal.configuration.SignalProxy

class ProxyValues(store: KeyValueStore) : SignalStoreValues(store) {

    companion object {
        private const val KEY_PROXY_ENABLED = "proxy.enabled"
        private const val KEY_HOST = "proxy.host"
        private const val KEY_PORT = "proxy.port"
    }
    override fun onFirstEverAppLaunch() {
    }

    override fun getKeysToIncludeInBackup(): List<String> {
        return listOf(KEY_PROXY_ENABLED, KEY_HOST, KEY_PORT)
    }

    fun enableProxy(proxy: SignalProxy) {
        require(proxy.host.isNotEmpty()) { "Empty host!" }

        store.beginWrite()
            .putBoolean(KEY_PROXY_ENABLED, true)
            .putString(KEY_HOST, proxy.host)
            .putInteger(KEY_PORT, proxy.port)
            .apply();
    }

    fun disableProxy() {
        putBoolean(KEY_PROXY_ENABLED, false)
    }

    fun isProxyEnabled(): Boolean {
        return getBoolean(KEY_PROXY_ENABLED, false)
    }

    fun setProxy(proxy: SignalProxy?) {
        if (proxy != null) {
            store.beginWrite()
                .putString(KEY_HOST, proxy.host)
                .putInteger(KEY_PORT, proxy.port)
                .apply()
        } else {
            store.beginWrite()
                .remove(KEY_HOST)
                .remove(KEY_PORT)
                .apply()
        }
    }

    fun getProxy(): SignalProxy? {
        val host = getString(KEY_HOST, null)
        val port = getInteger(KEY_PORT, 0)

        return if (host != null) {
            SignalProxy(host, port)
        } else {
            null
        }
    }

    fun getProxyHost(): String? {
        val proxy = getProxy()
        return proxy?.host
    }
}