package com.ick.kalambury.wordsrepository

import com.ick.kalambury.wordsrepository.migration.DataUpdater
import com.ick.kalambury.wordsrepository.model.Word
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface WordsRepository {

    /**
     * Can be used to dynamically update words manifest or word sets.
     */
    val updater: DataUpdater

    /**
     * Gets access to info about available words sets matching given parameters.
     * Also marks them as selected according to default data or user selection identified by [id]
     * if available; see [WordsRepository.saveSelectedSetIds].
     *
     * @param usage purpose of sets
     * @param language language of words in sets
     * @param id identifier of combination of set parameters. Used to uniquely describe
     * particular set of word sets used in given game mode.
     *
     * @return reactivex.Observable stream with sets matching given parameters.
     */
    fun getSetsData(usage: Usage, language: Language, id: Id): Observable<WordsSetInfo>

    /**
     * Prepares instance containing all word sets provided in [selection] parameter.
     * It's later used to provide words during game. It's durably persisted and updated
     * if [selection] changes to maximally expand period before words starts to repeat.
     *
     * Note: this function must be called before [WordsRepository.getWord] or [WordsRepository.requestWord],
     * otherwise [UninitializedInstanceException] will be throw.
     *
     * @param id identifier of combination of set parameters. Used to uniquely describe
     * particular set of word sets used in given game mode.
     * @param selection list of word sets ids that will be included in prepared instance.
     *
     * @return reactivex.Completable that completes when instance is prepared and ready to use.
     */
    fun prepareWordsInstance(id: Id, selection: List<String>): Completable

    /**
     * Persist currently used instance.
     *
     * @param id identifier of combination of set parameters. Used to uniquely describe
     * particular set of word sets used in given game mode.
     *
     * @return reactivex.Completable that completes when instance has been persisted durably.
     */
    fun saveWordsInstance(id: Id): Completable

    /**
     * Saves user choice of sets related with instance identified by [id].
     *
     * @param id identifier of combination of set parameters. Used to uniquely describe
     * particular set of word sets used in given game mode.
     * @param selection list of word sets ids
     *
     * @return reactivex.Completable that completes when selection has been persisted durably.
     */
    fun saveSelectedSetIds(id: Id, selection: List<String>): Completable

    /**
     * Gets single word from instance identified by [id].
     *
     * @return reactivex.Single with random word from random set available in instance.
     * @throws UninitializedInstanceException if instance was not prepared before with
     * [WordsRepository.prepareWordsInstance] fun.
     */
    fun getWord(id: Id): Single<Word>

    /**
     * Gets reactivex.Observable that will emit one random word for each call to
     * [WordsRepository.requestWord].
     *
     * @return stream of random words.
     * @throws UninitializedInstanceException if instance was not prepared before with
     * [WordsRepository.prepareWordsInstance] fun.
     */
    fun getWordsObservable(): Observable<Word>

    /**
     * Triggers emission of one random word from instance identified by [id].
     * Words will be emitted by Observable returned in [WordsRepository.getWordsObservable]
     *
     * @throws UninitializedInstanceException if instance was not prepared before with
     * [WordsRepository.prepareWordsInstance] fun.
     */
    fun requestWord(id: Id)

}