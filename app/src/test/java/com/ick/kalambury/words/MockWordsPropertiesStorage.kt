package com.ick.kalambury.words

import com.ick.kalambury.wordsrepository.properties.WordsPropertiesStorage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

class MockWordsPropertiesStorage(
    wordsRepositoryVersion: Int = 0,
    selectedSets: Set<String> = setOf(),
) : WordsPropertiesStorage
{

    private var _wordsRepositoryVersion = Flowable.just(wordsRepositoryVersion)
    override val wordsRepositoryVersion: Flowable<Int> = _wordsRepositoryVersion
    override fun setWordsRepositoryVersion(version: Int) {
        _wordsRepositoryVersion = Flowable.just(version)
    }

    private var _selectedSets = Flowable.just(selectedSets)
    override fun getSelectedWordsSets(instanceId: String): Flowable<Set<String>> = _selectedSets
    override fun setSelectedWordsSets(instanceId: String, sets: Set<String>): Completable {
        _selectedSets = Flowable.just(sets)
        return Completable.complete()
    }

}