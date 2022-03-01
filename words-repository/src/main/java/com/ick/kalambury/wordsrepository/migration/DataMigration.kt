package com.ick.kalambury.wordsrepository.migration

import com.ick.kalambury.wordsrepository.properties.WordsPropertiesStorage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface DataMigration {

    fun shouldMigrate(version: Int, properties: WordsPropertiesStorage): Single<Boolean>

    fun migrate(dataSource: CompositeDataSource): Completable

}