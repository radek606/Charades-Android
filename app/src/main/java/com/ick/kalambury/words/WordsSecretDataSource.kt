package com.ick.kalambury.words

import com.ick.kalambury.settings.EncryptionKeysStorage
import com.ick.kalambury.util.crypto.SecretDataSource
import io.reactivex.rxjava3.core.Flowable

class WordsSecretDataSource(private val storage: EncryptionKeysStorage) : SecretDataSource {

    override fun getEncryptedSecret(): Flowable<String> = storage.wordsEncryptedSecret
    override fun setEncryptedSecret(secretString: String?) {
        storage.setWordsEncryptedSecret(secretString)
    }

    override fun getUnencryptedSecret(): Flowable<String> = storage.wordsUnencryptedSecret
    override fun setUnencryptedSecret(secretString: String?) {
        storage.setWordsUnencryptedSecret(secretString)
    }

}