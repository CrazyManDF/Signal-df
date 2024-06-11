package org.thoughtcrime.securesms.crypto

import org.thoughtcrime.securesms.util.JsonUtils
import java.io.IOException

class AttachmentSecret {

    var classicCipherKey: ByteArray? = null


    var classicMacKey: ByteArray? = null

    var modernKey: ByteArray? = null
        private set

    constructor()
    constructor(
        classicCipherKey: ByteArray?,
        classicMacKey: ByteArray?,
        modernKey: ByteArray
    ) {
        this.classicCipherKey = classicCipherKey
        this.classicMacKey = classicMacKey
        this.modernKey = modernKey
    }

    fun serialize(): String{
        return ""
    }

    companion object {

        fun fromString(value: String): AttachmentSecret {
            try {
                return JsonUtils.fromJson(value, AttachmentSecret::class.java)
            } catch (e: IOException) {
                throw AssertionError(e)
            }
        }
    }

}