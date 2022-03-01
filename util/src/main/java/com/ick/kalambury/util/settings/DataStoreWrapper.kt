package com.ick.kalambury.util.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.rxjava3.RxDataStore
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

class DataStoreWrapper(private val dataStore: RxDataStore<Preferences>) : PreferencesStorage {

    override fun data(): Flowable<Preferences> {
        return dataStore.data()
    }

    override fun <T : Any> getValue(key: Preferences.Key<T>, defaultValue: T): Flowable<T> {
        return dataStore.data().map { it[key] ?: defaultValue }
    }

    override fun <T> setValue(key: Preferences.Key<T>, value: T?): Single<Preferences> {
        return dataStore.updateDataAsync {
            Single.fromCallable {
                it.toMutablePreferences().apply {
                    //this check is already done internally but needs to be repeated here
                    //because fun 'set()' accepts only non-null values
                    when(value) {
                        null -> remove(key)
                        else -> set(key, value)
                    }
                }.toPreferences()
            }
        }
    }

}