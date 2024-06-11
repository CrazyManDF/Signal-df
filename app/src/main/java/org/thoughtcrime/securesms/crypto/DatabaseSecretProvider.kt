package org.thoughtcrime.securesms.crypto

import android.content.Context
import android.os.Build
import org.thoughtcrime.securesms.crypto.KeyStoreHelper.SealedData
import org.thoughtcrime.securesms.util.TextSecurePreferences
import org.thoughtcrime.securesms.util.TextSecurePreferences.setDatabaseEncryptedSecret
import org.thoughtcrime.securesms.util.TextSecurePreferences.setDatabaseUnencryptedSecret
import java.io.IOException
import java.security.SecureRandom

object DatabaseSecretProvider {


    @Volatile
    var instance: DatabaseSecret? = null

    fun getOrCreateDatabaseSecret(context: Context): DatabaseSecret {
        if (instance == null){
            synchronized(DatabaseSecretProvider::class){
                if (instance == null){
                    instance = getOrCreate(context)
                }
            }
        }
        return instance!!
    }

    private fun getOrCreate(context: Context): DatabaseSecret {
        val unencryptedSecret = TextSecurePreferences.getDatabaseUnencryptedSecret(context)
        val encryptedSecret = TextSecurePreferences.getDatabaseEncryptedSecret(context)
        if (unencryptedSecret != null) return getUnencryptedDatabaseSecret(context, unencryptedSecret)
        else if (encryptedSecret != null) return getEncryptedDatabaseSecret(encryptedSecret)
        else return createAndStoreDatabaseSecret(context)
    }

    private fun getUnencryptedDatabaseSecret(context: Context , unencryptedSecret: String ): DatabaseSecret {
        try {
            val databaseSecret = DatabaseSecret(unencryptedSecret)
            if (Build.VERSION.SDK_INT < 23) {
                return databaseSecret
            } else {
                val encryptedSecret = KeyStoreHelper.seal(databaseSecret.asBytes())

                TextSecurePreferences.setDatabaseEncryptedSecret(context, encryptedSecret.serialize())
                TextSecurePreferences.setDatabaseUnencryptedSecret(context, null)

                return databaseSecret
            }
        } catch (e: IOException){
            throw AssertionError(e)
        }
    }

    private fun  getEncryptedDatabaseSecret(serializedEncryptedSecret: String): DatabaseSecret{
        if (Build.VERSION.SDK_INT < 23) {
            throw java.lang.AssertionError("OS downgrade not supported. KeyStore sealed data exists on platform < M!")
        } else {
            val encryptedSecret = SealedData.fromString(serializedEncryptedSecret)
            return DatabaseSecret(KeyStoreHelper.unseal(encryptedSecret))
        }
    }

    private fun createAndStoreDatabaseSecret(context: Context): DatabaseSecret {
        val random = SecureRandom()
        val secret = ByteArray(32)
        random.nextBytes(secret)

        val databaseSecret = DatabaseSecret(secret)
        if (Build.VERSION.SDK_INT >= 23) {
            val encryptedSecret = KeyStoreHelper.seal(databaseSecret.asBytes())
            setDatabaseEncryptedSecret(context, encryptedSecret.serialize())
        } else {
            setDatabaseUnencryptedSecret(context, databaseSecret.asString())
        }

        return databaseSecret
    }
}