package org.whispersystems.signalservice.api.push

import java.io.InputStream

interface TrustStore {
    fun getKeyStoreInputStream(): InputStream
    fun getKeyStorePassword(): String
}