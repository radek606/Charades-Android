package com.ick.kalambury.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreHelper {

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "KalamburySecret"

    @RequiresApi(Build.VERSION_CODES.M)
    fun seal(input: ByteArray): SealedData {
        val secretKey = getOrCreateKeyStoreEntry()

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val data = cipher.doFinal(input)

            SealedData(iv, data)
        } catch (e: Exception) {
            throw AssertionError(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun unseal(sealedData: SealedData): ByteArray {
        val secretKey = getKeyStoreEntry()
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, sealedData.iv))
            cipher.doFinal(sealedData.data)
        } catch (e: Exception) {
            throw AssertionError(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOrCreateKeyStoreEntry(): SecretKey {
        return if (hasKeyStoreEntry()) getKeyStoreEntry() else createKeyStoreEntry()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createKeyStoreEntry(): SecretKey {
        return try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            throw AssertionError(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyStoreEntry(): SecretKey {
        val keyStore = getKeyStore()
        return try {
            // Attempt 1
            getSecretKey(keyStore)
        } catch (e: UnrecoverableKeyException) {
            try {
                // Attempt 2
                getSecretKey(keyStore)
            } catch (e2: UnrecoverableKeyException) {
                throw AssertionError(e2)
            }
        }
    }

    private fun getSecretKey(keyStore: KeyStore): SecretKey {
        return try {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as SecretKeyEntry
            entry.secretKey
        } catch (e: UnrecoverableKeyException) {
            throw e
        } catch (e: Exception) {
            throw AssertionError(e)
        }
    }

    private fun getKeyStore(): KeyStore {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            keyStore
        } catch (e: Exception) {
            throw AssertionError(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasKeyStoreEntry(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)

            keyStore.containsAlias(KEY_ALIAS) and keyStore.entryInstanceOf(
                KEY_ALIAS,
                SecretKeyEntry::class.java
            )
        } catch (e: Exception) {
            throw AssertionError(e)
        }
    }
}

@Keep
@Suppress("ArrayInDataClass")
data class SealedData(
    @JsonSerialize(using = ByteArraySerializer::class)
    @JsonDeserialize(using = ByteArrayDeserializer::class)
    val iv: ByteArray,

    @JsonSerialize(using = ByteArraySerializer::class)
    @JsonDeserialize(using = ByteArrayDeserializer::class)
    val data: ByteArray,
) {
    fun serialize(): String = JsonUtils.toJson(this)

    companion object {

        fun fromString(jsonString: String) = JsonUtils.fromJson(jsonString, SealedData::class.java)

    }
}