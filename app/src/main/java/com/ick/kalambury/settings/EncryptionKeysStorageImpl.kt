package com.ick.kalambury.settings

import com.ick.kalambury.util.settings.DataStoreWrapper
import com.ick.kalambury.util.settings.PreferencesStorage
import io.reactivex.rxjava3.core.Flowable

interface EncryptionKeysStorage {
    val wordsEncryptedSecret: Flowable<String>
    fun setWordsEncryptedSecret(secretString: String?)

    val wordsUnencryptedSecret: Flowable<String>
    fun setWordsUnencryptedSecret(secretString: String?)
}

class EncryptionKeysStorageImpl(
    private val dataStore: DataStoreWrapper,
    private val keys: PreferenceKeys,
) : EncryptionKeysStorage, PreferencesStorage by dataStore {

    override val wordsEncryptedSecret: Flowable<String>
        get() = getValue(keys.wordsEncryptedSecret, "")
    override fun setWordsEncryptedSecret(secretString: String?) {
        setValue(keys.wordsEncryptedSecret, secretString)
    }

    override val wordsUnencryptedSecret: Flowable<String>
        get() = getValue(keys.wordsUnencryptedSecret, "")
    override fun setWordsUnencryptedSecret(secretString: String?) {
        setValue(keys.wordsUnencryptedSecret, secretString)
    }

}