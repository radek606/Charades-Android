package com.ick.kalambury.util.crypto

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESCipherStreamsFactory {

    fun create(
        inputStream: InputStream,
        secret: Secret,
        transformation: String = "AES/CTR/NoPadding",
    ): InputStream =
        CipherInputStream(inputStream, initCipher(Cipher.DECRYPT_MODE, secret, transformation))

    fun create(
        outputStream: OutputStream,
        secret: Secret,
        transformation: String = "AES/CTR/NoPadding",
    ): OutputStream =
        CipherOutputStream(outputStream, initCipher(Cipher.ENCRYPT_MODE, secret, transformation))

    private fun initCipher(mode: Int, secret: Secret, transformation: String): Cipher {
        return try {
            Cipher.getInstance(transformation).apply {
                init(mode, SecretKeySpec(secret.key, "AES"), IvParameterSpec(secret.iv))
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}