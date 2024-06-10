package org.thoughtcrime.securesms.crypto

class AttachmentSecret {

    var classicCipherKey: ByteArray? = null


    var classicMacKey: ByteArray? = null

    var modernKey: ByteArray? = null
        private set

    constructor()
    constructor(
        classicCipherKey: ByteArray,
        classicMacKey: ByteArray,
        modernKey: ByteArray
    ) {
        this.classicCipherKey = classicCipherKey
        this.classicMacKey = classicMacKey
        this.modernKey = modernKey
    }

    fun serialize(): String{
        return ""
    }

//    fun fromString(value: String): AttachmentSecret {
//
//    }

}