package com.ick.kalambury.wordsrepository.properties

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.ick.kalambury.util.settings.DataStoreWrapper
import com.ick.kalambury.util.settings.PreferencesStorage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

interface WordsPropertiesStorage {

    val wordsRepositoryVersion: Flowable<Int>
    fun setWordsRepositoryVersion(version: Int)

    fun getSelectedWordsSets(instanceId: String): Flowable<Set<String>>
    fun setSelectedWordsSets(instanceId: String, sets: Set<String>): Completable

}

internal class WordsPropertiesStorageImpl(
    private val dataStore: DataStoreWrapper,
) : WordsPropertiesStorage, PreferencesStorage by dataStore {

    override val wordsRepositoryVersion: Flowable<Int>
        get() = getValue(KEY_VERSION, 0)
    override fun setWordsRepositoryVersion(version: Int) {
        setValue(KEY_VERSION, version)
    }

    override fun getSelectedWordsSets(instanceId: String): Flowable<Set<String>> {
        return getValue(stringSetPreferencesKey("selected_sets_$instanceId"), setOf())
    }
    override fun setSelectedWordsSets(instanceId: String, sets: Set<String>): Completable {
        return setValue(stringSetPreferencesKey("selected_sets_$instanceId"), sets)
            .flatMapCompletable { Completable.complete() }
    }

    companion object {

        internal val KEY_VERSION = intPreferencesKey("words_repo_version")

    }

}