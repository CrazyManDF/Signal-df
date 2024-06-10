package org.thoughtcrime.securesms.crypto

import org.signal.libsignal.protocol.util.Hex
import java.io.IOException

class DatabaseSecret {

    private var key: ByteArray

    private var encoded: String

    constructor(key: ByteArray) {
        this.key = key
        this.encoded = Hex.toStringCondensed(key)
    }

    @Throws(IOException::class)
    constructor(encoded: String) {
        this.key = Hex.fromStringCondensed(encoded)
        this.encoded = encoded
    }

    fun asString(): String {
        return encoded
    }

    fun asBytes(): ByteArray {
        return key
    }
}