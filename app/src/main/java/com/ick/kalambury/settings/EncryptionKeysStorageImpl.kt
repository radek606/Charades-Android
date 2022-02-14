package com.ick.kalambury.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataStore
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface EncryptionKeysStorage {
    val wordsEncryptedSecret: Flowable<String>
    fun setWordsEncryptedSecret(secretString: String?)

    val wordsUnencryptedSecret: Flowable<String>
    fun setWordsUnencryptedSecret(secretString: String?)
}

class EncryptionKeysStorageImpl(
    private val dataStore: RxDataStore<Preferences>,
    private val keys: PreferenceKeysProvider,
) : EncryptionKeysStorage {

    private fun <T> getValue(key: Preferences.Key<T>, defaultValue: T): Flowable<T> {
        return dataStore.data().map { it[key] ?: defaultValue }
    }

    private fun <T> setValue(key: Preferences.Key<T>, value: T?) {
        dataStore.updateDataAsync {
            Single.fromCallable {
                it.toMutablePreferences().apply {
                    when(value) {
                        null -> remove(key)
                        else -> set(key, value)
                    }
                }
            }
        }
    }

    override val wordsEncryptedSecret: Flowable<String> =
        getValue(keys.wordsEncryptedSecret.asDataStoreKey(), "")
    override fun setWordsEncryptedSecret(secretString: String?) =
        setValue(keys.wordsEncryptedSecret.asDataStoreKey(), secretString)

    override val wordsUnencryptedSecret: Flowable<String> =
        getValue(keys.wordsUnencryptedSecret.asDataStoreKey(), "")
    override fun setWordsUnencryptedSecret(secretString: String?) =
        setValue(keys.wordsUnencryptedSecret.asDataStoreKey(), secretString)

}