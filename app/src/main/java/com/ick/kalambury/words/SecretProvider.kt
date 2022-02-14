package com.ick.kalambury.words

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.KeyGenerator

interface SecretProvider {

    fun getOrCreateSecret(): Secret

    fun generateKey(n: Int): ByteArray {
        try {
            val generator = KeyGenerator.getInstance("AES")
            generator.init(n)
            return generator.generateKey().encoded
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    fun generateIv(): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }

}