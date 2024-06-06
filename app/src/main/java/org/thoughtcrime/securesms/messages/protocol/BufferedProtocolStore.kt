package org.thoughtcrime.securesms.messages.protocol

class BufferedProtocolStore {


    fun flushToDisk() {

    }
    companion object {
        fun create(): BufferedProtocolStore {
            return BufferedProtocolStore()
        }
    }
}