package org.thoughtcrime.securesms.crypto

import android.content.Context
import android.os.Build
import org.thoughtcrime.securesms.crypto.KeyStoreHelper.SealedData
import org.thoughtcrime.securesms.util.TextSecurePreferences
import org.thoughtcrime.securesms.util.TextSecurePreferences.setAttachmentEncryptedSecret
import org.thoughtcrime.securesms.util.TextSecurePreferences.setAttachmentUnencryptedSecret
import java.security.SecureRandom

class AttachmentSecretProvider private constructor(context: Context){

    private val context = context.applicationContext

    private var attachmentSecret: AttachmentSecret? = null

    companion object {

        private var provider: AttachmentSecretProvider? = null
        @Synchronized
        fun getInstance(context: Context): AttachmentSecretProvider {
            if (provider == null)
                provider = AttachmentSecretProvider(context.applicationContext)
            return provider!!
        }
    }

    @Synchronized
    fun getOrCreateAttachmentSecret(): AttachmentSecret {
        attachmentSecret?.let {
            return it
        }

        val unencryptedSecret: String? =
            TextSecurePreferences.getAttachmentUnencryptedSecret(context)
        val encryptedSecret: String? = TextSecurePreferences.getAttachmentEncryptedSecret(context)

        if (unencryptedSecret != null) attachmentSecret =
            getUnencryptedAttachmentSecret(context, unencryptedSecret)
        else if (encryptedSecret != null) attachmentSecret =
            getEncryptedAttachmentSecret(encryptedSecret)
        else attachmentSecret = createAndStoreAttachmentSecret(context)

        return attachmentSecret!!
    }

    private fun getUnencryptedAttachmentSecret(
        context: Context,
        unencryptedSecret: String
    ): AttachmentSecret {
        val attachmentSecret: AttachmentSecret = AttachmentSecret.fromString(unencryptedSecret)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return attachmentSecret
        } else {
            val encryptedSecret = KeyStoreHelper.seal(attachmentSecret.serialize().toByteArray())

            TextSecurePreferences.setAttachmentEncryptedSecret(context, encryptedSecret.serialize())
            TextSecurePreferences.setAttachmentUnencryptedSecret(context, null)

            return attachmentSecret
        }
    }

    private fun getEncryptedAttachmentSecret(serializedEncryptedSecret: String): AttachmentSecret {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            throw AssertionError("OS downgrade not supported. KeyStore sealed data exists on platform < M!")
        } else {
            val encryptedSecret = SealedData.fromString(serializedEncryptedSecret)
            return AttachmentSecret.fromString(String(KeyStoreHelper.unseal(encryptedSecret)))
        }
    }

    private fun createAndStoreAttachmentSecret(context: Context): AttachmentSecret {
        val random = SecureRandom()
        val secret = ByteArray(32)
        random.nextBytes(secret)

        val attachmentSecret = AttachmentSecret(null, null, secret)
        storeAttachmentSecret(context, attachmentSecret)

        return attachmentSecret
    }

    private fun storeAttachmentSecret(context: Context, attachmentSecret: AttachmentSecret) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val encryptedSecret = KeyStoreHelper.seal(attachmentSecret.serialize().toByteArray())
            setAttachmentEncryptedSecret(context, encryptedSecret.serialize())
        } else {
            setAttachmentUnencryptedSecret(context, attachmentSecret.serialize())
        }
    }
}