package com.ick.kalambury.words

import android.os.Build
import androidx.annotation.WorkerThread
import com.ick.kalambury.logging.Log
import com.ick.kalambury.settings.EncryptionKeysStorage
import com.ick.kalambury.util.KeyStoreHelper
import com.ick.kalambury.util.SealedData
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordsSecretProvider @Inject constructor(private val storage: EncryptionKeysStorage) :
    SecretProvider {

    private val secret: Secret by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { getOrCreateSecretInternal() }

    @WorkerThread
    override fun getOrCreateSecret(): Secret = secret

    private fun getOrCreateSecretInternal(): Secret {
        val (encrypted, unencrypted) = Single.zip(
            storage.wordsEncryptedSecret.firstOrError(),
            storage.wordsUnencryptedSecret.firstOrError(),
            ::Pair
        ).blockingGet()

        Log.d(logTag(), "getOrCreateSecretInternal()")

        return if (!unencrypted.isNullOrBlank()) {
            getUnencryptedSecret(unencrypted)
        } else if (!encrypted.isNullOrBlank()) {
            getEncryptedSecret(encrypted)
        } else {
            createAndStoreSecret()
        }
    }

    private fun getUnencryptedSecret(secretString: String): Secret {
        Log.d(logTag(), "getUnencryptedSecret()")

        val secret = Secret.fromString(secretString)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val encryptedSecret = KeyStoreHelper.seal(secret.serialize().encodeToByteArray())

            storage.apply {
                setWordsEncryptedSecret(encryptedSecret.serialize())
                setWordsUnencryptedSecret(null)
            }
        }

        return secret
    }

    private fun getEncryptedSecret(secretString: String): Secret {
        Log.d(logTag(), "getEncryptedSecret()")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            throw RuntimeException("OS downgrade not supported. KeyStore sealed data exists on platform < M!")
        } else {
            val encryptedSecret = SealedData.fromString(secretString)
            return Secret.fromString(String(KeyStoreHelper.unseal(encryptedSecret)))
        }
    }

    private fun createAndStoreSecret(): Secret {
        Log.d(logTag(), "createAndStoreSecret()")

        val secret = Secret(generateKey(256), generateIv())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val encryptedSecret = KeyStoreHelper.seal(secret.serialize().encodeToByteArray())
            storage.setWordsEncryptedSecret(encryptedSecret.serialize())
        } else {
            storage.setWordsUnencryptedSecret(secret.serialize())
        }

        return secret
    }

}