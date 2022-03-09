package com.ick.kalambury.util.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.ick.kalambury.util.ByteArrayBase64Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreHelper {

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "KalamburySecret"

    @RequiresApi(Build.VERSION_CODES.M)
    fun seal(input: ByteArray): SealedData {
        return Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.ENCRYPT_MODE, getOrCreateKeyStoreEntry())

            SealedData(iv, doFinal(input))
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun unseal(sealedData: SealedData): ByteArray {
        return Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, getKeyStoreEntry(), GCMParameterSpec(128, sealedData.iv))
            doFinal(sealedData.data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOrCreateKeyStoreEntry(): SecretKey {
        return if (hasKeyStoreEntry()) getKeyStoreEntry() else createKeyStoreEntry()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createKeyStoreEntry(): SecretKey {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).run {
            init(keyGenParameterSpec)
            generateKey()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyStoreEntry() = getSecretKey(getKeyStore())

    private fun getSecretKey(keyStore: KeyStore): SecretKey {
        return (keyStore.getEntry(KEY_ALIAS, null) as SecretKeyEntry).secretKey
    }

    private fun getKeyStore() = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasKeyStoreEntry(): Boolean {
        return  getKeyStore().run {
            containsAlias(KEY_ALIAS) and entryInstanceOf(KEY_ALIAS, SecretKeyEntry::class.java)
        }
    }
}

@Suppress("ArrayInDataClass")
@Serializable
data class SealedData(
    @Serializable(with = ByteArrayBase64Serializer::class)
    val iv: ByteArray,
    @Serializable(with = ByteArrayBase64Serializer::class)
    val data: ByteArray,
) {
    fun serialize(): String = Json.encodeToString(this)

    companion object {

        fun fromString(jsonString: String) = Json.decodeFromString<SealedData>(jsonString)

    }
}