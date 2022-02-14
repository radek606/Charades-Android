package com.ick.kalambury.words

import com.ick.kalambury.GameMode
import com.ick.kalambury.list.model.WordsSetData
import com.ick.kalambury.logging.Log
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.internal.toImmutableList
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WordsRepository @Inject constructor(
    @Named("wordsManifestLocalDataSource") private val manifestDataSource: WordsManifestDataSource,
    private val wordsSetDataSource: WordsSetDataSource,
    private val wordsInstanceDataSource: WordsInstanceDataSource,
    private val mainPreferenceStorage: MainPreferenceStorage,
    private val schedulerProvider: SchedulerProvider,
) {

    private val setsInfoList: MutableList<WordsSetInfo> = Collections.synchronizedList(mutableListOf())
    private val instanceMap: MutableMap<String, WordsInstance> = mutableMapOf()

    private val wordsRequest: PublishSubject<String> = PublishSubject.create()

    private fun getWordsSetInfo(): Observable<WordsSetInfo> {
        return if (setsInfoList.isNotEmpty()) {
            Observable.fromIterable(setsInfoList.toImmutableList())
        } else {
            manifestDataSource.getWordsManifest()
                .map { it.sets.map(::WordsSetInfo) }
                .doOnSuccess {
                    setsInfoList.clear()
                    setsInfoList.addAll(it)
                }
                .flatMapObservable { Observable.fromIterable(it) }
        }
    }

    fun updateManifest(manifest: WordsManifest): Completable {
        return manifestDataSource.saveWordsManifest(manifest)
            .doOnComplete {
                setsInfoList.clear()
                setsInfoList.addAll(manifest.sets.map(::WordsSetInfo))
            }
    }

    val hasNewSets: Single<Boolean>
        get() = getWordsSetInfo().any(WordsSetInfo::isNew)

    val hasUpdatedSets: Single<Boolean>
        get() = getWordsSetInfo().any(WordsSetInfo::isUpdated)

    fun getSetsData(gameMode: GameMode, language: Language): Single<List<WordsSetData>> {
        return getWordsSetInfo()
            .filter { it.language == language && it.isEligible(gameMode) }
            .toList()
            .zipWith(getSelectedSetIds(gameMode, language)) { sets, selectedIds ->
                if (selectedIds.isEmpty()) {
                    sets.map { WordsSetData(it, it.isDefault(gameMode)) }
                } else {
                    sets.map { WordsSetData(it, selectedIds.contains(it.id)) }
                }
            }
    }

    fun prepareWordsInstance(gameMode: GameMode, language: Language, selection: List<String>): Completable {
        return getWordsInstance(gameMode, language)
            .onErrorResumeNext { loadWordsInstance(gameMode, language) }
            .onErrorResumeNext { createWordsInstance(gameMode, language) }
            .flatMap { instance ->
                val setsToRemove = instance.selectedSets.filter { !selection.contains(it) }
                val setsToAdd = selection.filter { !instance.selectedSets.contains(it) }

                instance.selectedSets = selection
                instance.wordsSets.removeIf { setsToRemove.contains(it.id) }

                if (setsToAdd.isNotEmpty()) {
                    Single.zip(Single.just(instance), loadWordsSets(setsToAdd)) { ins, sets ->
                        ins.wordsSets.addAll(sets)
                        ins.reset()
                        ins
                    }
                } else {
                    Single.just(instance)
                }
            }
            .doOnSuccess { instanceMap[it.id] = it }
            .flatMapCompletable { saveSelectedSetIds(gameMode, language, selection) }
    }

    fun saveWordsInstance(gameMode: GameMode, language: Language): Completable {
        return instanceMap[assembleInstanceId(gameMode, language)]?.let {
            wordsInstanceDataSource.saveWordsInstance(it)
        } ?: Completable.complete()
    }

    fun saveSelectedSetIds(gameMode: GameMode, language: Language, selectedSets: List<String>): Completable {
        return mainPreferenceStorage.setSelectedWordsSets(assembleInstanceId(gameMode, language), selectedSets)
    }

    fun getWord(gameMode: GameMode, language: Language): Single<Word> {
        return instanceMap[assembleInstanceId(gameMode, language)]?.let { instance ->
            instance.getNextWord()
                .onErrorResumeNext {
                    resetWordsInstance(instance)
                        .flatMap { it.getNextWord() }
                }
        } ?: Single.error(UninitializedInstanceException())
    }

    fun requestWord(gameMode: GameMode, language: Language) {
        wordsRequest.onNext(assembleInstanceId(gameMode, language))
    }

    fun getWordsObservable(): Observable<Word> {
        return wordsRequest.switchMap(::drawWord)
    }

    private fun drawWord(instanceId: String): Observable<Word> {
        return instanceMap[instanceId]?.let { instance ->
            instance.wordsObservable
                .onErrorResumeNext {
                    resetWordsInstance(instance)
                        .toObservable()
                        .flatMap { it.wordsObservable }
                }
        } ?: Observable.error(UninitializedInstanceException())
    }

    private fun resetWordsInstance(instance: WordsInstance): Single<WordsInstance> {
        return loadWordsSets(instance.selectedSets)
            .map {
                instance.wordsSets.addAll(it)
                instance.reset()
                instance
            }
    }

    private fun loadWordsSets(setIds: Collection<String>): Single<List<WordsSet>> {
        return Observable.fromIterable(setIds)
            .flatMap {
                wordsSetDataSource.getWordsSet(it)
                    .subscribeOn(schedulerProvider.io())
                    .toObservable()
            }
            .toList()
    }

    private fun getWordsInstance(gameMode: GameMode, language: Language): Single<WordsInstance> {
        return instanceMap[assembleInstanceId(gameMode, language)]?.let { Single.just(it) }
            ?: Single.error(UninitializedInstanceException())
    }

    private fun loadWordsInstance(gameMode: GameMode, language: Language): Single<WordsInstance> {
        return wordsInstanceDataSource.getWordsInstance(assembleInstanceId(gameMode, language))
            .doOnSuccess { Log.d(logTag(), "Instance: ${assembleInstanceId(gameMode, language)} loaded from storage.") }
    }

    private fun createWordsInstance(gameMode: GameMode, language: Language): Single<WordsInstance> {
        return Single.fromCallable { WordsInstance(assembleInstanceId(gameMode, language)) }
            .doOnSuccess { Log.d(logTag(), "Created instance: ${assembleInstanceId(gameMode, language)}") }
    }

    private fun getSelectedSetIds(gameMode: GameMode, language: Language): Single<List<String>> {
        val instanceId = assembleInstanceId(gameMode, language)
        return mainPreferenceStorage.getSelectedWordsSets(instanceId)
            .onErrorReturn { listOf() }
            .firstOrError()
    }

    private fun assembleInstanceId(gameMode: GameMode, language: Language): String {
        return gameMode.name + "_" + language.name
    }

}

internal class UninitializedInstanceException : RuntimeException()