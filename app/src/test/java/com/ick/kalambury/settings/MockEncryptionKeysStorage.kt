package com.ick.kalambury.settings

import io.reactivex.rxjava3.core.Flowable

class MockEncryptionKeysStorage(
    wordsEncryptedSecret: String,
    wordsUnencryptedSecret: String,
) : EncryptionKeysStorage {

    private var _wordsEncryptedSecret = Flowable.just(wordsEncryptedSecret)
    override val wordsEncryptedSecret: Flowable<String> = _wordsEncryptedSecret
    override fun setWordsEncryptedSecret(secretString: String?) {
        _wordsEncryptedSecret = Flowable.just(secretString ?: "")
    }

    private var _wordsUnencryptedSecret = Flowable.just(wordsUnencryptedSecret)
    override val wordsUnencryptedSecret: Flowable<String> = _wordsUnencryptedSecret
    override fun setWordsUnencryptedSecret(secretString: String?) {
        _wordsUnencryptedSecret = Flowable.just(secretString ?: "")
    }

}