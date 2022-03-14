package com.ick.kalambury.wordsrepository

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.wordsrepository.datasource.WordsInstanceDataSource
import com.ick.kalambury.wordsrepository.datasource.WordsManifestDataSource
import com.ick.kalambury.wordsrepository.datasource.WordsSetDataSource
import com.ick.kalambury.wordsrepository.migration.CompositeDataSource
import com.ick.kalambury.wordsrepository.migration.DataMigration
import com.ick.kalambury.wordsrepository.migration.DataUpdater
import com.ick.kalambury.wordsrepository.migration.WordsDataMigrator
import com.ick.kalambury.wordsrepository.model.Word
import com.ick.kalambury.wordsrepository.model.WordsInstance
import com.ick.kalambury.wordsrepository.model.WordsManifest
import com.ick.kalambury.wordsrepository.model.WordsSet
import com.ick.kalambury.wordsrepository.properties.WordsPropertiesStorage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.SingleSubject
import java.io.File
import java.util.*

class WordsRepositoryImpl @VisibleForTesting(otherwise = PACKAGE_PRIVATE) constructor(
    rootDirectory: () -> File,
    private val manifestDataSource: WordsManifestDataSource,
    private val wordsSetDataSource: WordsSetDataSource,
    private val wordsInstanceDataSource: WordsInstanceDataSource,
    private val wordsPropertiesStorage: WordsPropertiesStorage,
    private val scheduler: Scheduler,
    migrations: List<DataMigration> = emptyList(),
) : WordsRepository {

    private val setsInfoList: MutableList<WordsManifest.Set> =
        Collections.synchronizedList(mutableListOf())
    private val instanceMap: MutableMap<String, WordsInstance> = mutableMapOf()

    private val migrationCompleteSignal: SingleSubject<Unit> = SingleSubject.create()
    private val updateSignal: PublishSubject<Unit> = PublishSubject.create()
    private val wordsRequest: PublishSubject<String> = PublishSubject.create()

    override val updater = DataUpdater(
        rootDirectory,
        manifestDataSource,
        wordsSetDataSource,
        migrationCompleteSignal,
        updateSignal,
    )

    init {
        WordsDataMigrator(
            CompositeDataSource(rootDirectory, manifestDataSource, wordsSetDataSource),
            wordsPropertiesStorage
        )
            .performMigration(migrations)
            .subscribeOn(scheduler)
            .subscribe { migrationCompleteSignal.onSuccess(Unit) }

        updateSignal.subscribeOn(scheduler)
            .subscribe { setsInfoList.clear() }
    }

    private fun getWordsSetInfo(): Observable<WordsManifest.Set> {
        return if (setsInfoList.isNotEmpty()) {
            Observable.fromIterable(setsInfoList.toList())
                .subscribeOn(scheduler)
        } else {
            migrationCompleteSignal
                .subscribeOn(scheduler)
                .flatMap { manifestDataSource.getLocalWordsManifest() }
                .doOnError {
                    Log.d(logTag, "Words manifest not found in local storage. " +
                        "Getting default from assets...")
                }
                .onErrorResumeNext { manifestDataSource.getAssetsWordsManifest() }
                .doOnSuccess { setsInfoList.addAll(it.sets) }
                .flatMapObservable { Observable.fromIterable(it.sets) }
        }
    }

    val hasNewSets: Single<Boolean>
        get() = getWordsSetInfo().any(WordsManifest.Set::isNew)

    val hasUpdatedSets: Single<Boolean>
        get() = getWordsSetInfo().any(WordsManifest.Set::isUpdated)

    override fun getSetsData(usage: Usage, language: Language, id: Id): Observable<WordsSetInfo> {
        return getWordsSetInfo()
            .filter { it.language == language && it.isEligible(usage) }
            .toList()
            .zipWith(getSelectedSetIds(id)) { sets, selectedIds ->
                if (selectedIds.isEmpty()) {
                    sets.map { WordsSetInfo(it, it.isDefault(usage)) }
                } else {
                    sets.map { WordsSetInfo(it, selectedIds.contains(it.id)) }
                }
            }.flatMapObservable { Observable.fromIterable(it) }
    }

    override fun prepareWordsInstance(id: Id, selection: List<String>): Completable {
        return migrationCompleteSignal
            .subscribeOn(scheduler)
            .flatMap { getWordsInstance(id) }
            .onErrorResumeNext { loadWordsInstance(id) }
            .onErrorResumeNext { createWordsInstance(id) }
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
            .flatMapCompletable { saveSelectedSetIds(id, selection) }
    }

    override fun saveWordsInstance(id: Id): Completable {
        return instanceMap[id.getId()]?.let {
            wordsInstanceDataSource.saveWordsInstance(it)
                .subscribeOn(scheduler)
        } ?: Completable.complete()
    }

    override fun saveSelectedSetIds(id: Id, selection: List<String>): Completable {
        return wordsPropertiesStorage.setSelectedWordsSets(
            id.getId(),
            selection.toSet()
        ).subscribeOn(scheduler)
    }

    override fun getWord(id: Id): Single<Word> {
        return instanceMap[id.getId()]?.let { instance ->
            instance.getNextWord()
                .onErrorResumeNext {
                    resetWordsInstance(instance)
                        .flatMap { it.getNextWord() }
                }
        } ?: Single.error(UninitializedInstanceException())
    }

    override fun requestWord(id: Id) {
        wordsRequest.onNext(id.getId())
    }

    override fun getWordsObservable(): Observable<Word> {
        return wordsRequest.switchMap(::drawWord)
    }

    private fun drawWord(id: String): Observable<Word> {
        return instanceMap[id]?.let { instance ->
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
            .flatMap { setId ->
                wordsSetDataSource.getLocalWordsSet(setId)
                    .doOnError { Log.d(logTag, "Failed loading words set from local storage. Getting fallback assets file...")}
                    .onErrorResumeNext { wordsSetDataSource.getAssetsWordsSet(setId) }
                    .subscribeOn(scheduler)
                    .toObservable()
            }
            .toList()
    }

    private fun getWordsInstance(id: Id): Single<WordsInstance> {
        return instanceMap[id.getId()]?.let { Single.just(it) }
            ?: Single.error(UninitializedInstanceException())
    }

    private fun loadWordsInstance(id: Id): Single<WordsInstance> {
        return wordsInstanceDataSource.getWordsInstance(id.getId())
            .doOnSuccess { Log.d(logTag, "Instance: $id loaded from storage.") }
    }

    private fun createWordsInstance(id: Id): Single<WordsInstance> {
        return Single.fromCallable { WordsInstance(id.getId()) }
            .doOnSuccess { Log.d(logTag, "Created instance: $id") }
    }

    private fun getSelectedSetIds(id: Id): Single<Set<String>> {
        return wordsPropertiesStorage.getSelectedWordsSets(id.getId())
            .firstOrError()
    }

}

internal class UninitializedInstanceException : RuntimeException()