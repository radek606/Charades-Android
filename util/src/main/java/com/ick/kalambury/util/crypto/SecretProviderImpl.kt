package com.ick.kalambury.util.crypto

import android.os.Build
import androidx.annotation.WorkerThread
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.KeyGenerator

class SecretProviderImpl(private val storage: SecretDataSource) : SecretProvider {

    private val secret: Secret by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { getOrCreateSecretInternal() }

    @WorkerThread
    override fun getOrCreateSecret(): Maybe<Secret> = Maybe.just(secret)

    private fun getOrCreateSecretInternal(): Secret {
        return Single.zip(
            storage.getEncryptedSecret().firstOrError(),
            storage.getUnencryptedSecret().firstOrError()
        ) { encrypted, unencrypted ->
            when {
                unencrypted.isNotBlank() -> getUnencryptedSecret(unencrypted)
                encrypted.isNotBlank() -> getEncryptedSecret(encrypted)
                else -> createAndStoreSecret()
            }
        }.blockingGet()
    }

    private fun getUnencryptedSecret(secretString: String): Secret {
        val secret = Secret.fromJson(secretString)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val encryptedSecret = KeyStoreHelper.seal(secret.serialize().encodeToByteArray())

            storage.apply {
                setEncryptedSecret(encryptedSecret.serialize())
                setUnencryptedSecret(null)
            }
        }

        return secret
    }

    private fun getEncryptedSecret(secretString: String): Secret {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            throw RuntimeException("OS downgrade not supported. KeyStore sealed data exists on platform < M!")
        } else {
            val encryptedSecret = SealedData.fromString(secretString)
            return Secret.fromJson(String(KeyStoreHelper.unseal(encryptedSecret)))
        }
    }

    private fun createAndStoreSecret(): Secret {
        val secret = Secret(generateKey(256), generateIv())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val encryptedSecret = KeyStoreHelper.seal(secret.serialize().encodeToByteArray())
            storage.setEncryptedSecret(encryptedSecret.serialize())
        } else {
            storage.setUnencryptedSecret(secret.serialize())
        }

        return secret
    }

    private fun generateKey(n: Int): ByteArray {
        try {
            val generator = KeyGenerator.getInstance("AES")
            generator.init(n)
            return generator.generateKey().encoded
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    private fun generateIv(): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }

}