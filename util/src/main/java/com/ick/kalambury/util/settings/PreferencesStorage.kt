package com.ick.kalambury.util.settings

import androidx.datastore.preferences.core.Preferences
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface PreferencesStorage {

    fun data(): Flowable<Preferences>

    fun <T : Any> getValue(key: Preferences.Key<T>, defaultValue: T): Flowable<T>
    fun <T> setValue(key: Preferences.Key<T>, value: T?): Single<Preferences>

}