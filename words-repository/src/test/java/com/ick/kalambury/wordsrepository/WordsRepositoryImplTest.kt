package com.ick.kalambury.wordsrepository

import com.ick.kalambury.wordsrepository.datasource.MockWordsInstanceDataSource
import com.ick.kalambury.wordsrepository.datasource.MockWordsManifestDataSource
import com.ick.kalambury.wordsrepository.datasource.MockWordsPropertiesStorage
import com.ick.kalambury.wordsrepository.datasource.MockWordsSetDataSource
import com.ick.kalambury.wordsrepository.model.Word
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import java.io.File

class WordsRepositoryImplTest {

    private lateinit var repository: WordsRepositoryImpl

    @Before
    fun init() {
        repository = WordsRepositoryImpl(
            rootDirectory = { File("") },
            manifestDataSource = MockWordsManifestDataSource(),
            wordsSetDataSource = MockWordsSetDataSource(),
            wordsInstanceDataSource = MockWordsInstanceDataSource(),
            wordsPropertiesStorage = MockWordsPropertiesStorage(),
            scheduler = Schedulers.trampoline(),
            migrations = listOf()
        )
    }

    @Test
    fun `load sets`() {
        val testSubscriber: TestObserver<WordsSetInfo> = TestObserver()

        repository.getSetsData(Usage.DRAWING, Language.PL) { "${Usage.DRAWING}_${Language.PL}" }
            .blockingSubscribe(testSubscriber)

        testSubscriber.assertComplete()
        testSubscriber.assertValueCount(3)
    }

    @Test
    fun `has new sets`() {
        val testSubscriber: TestObserver<Boolean> = TestObserver()

        repository.hasNewSets.blockingSubscribe(testSubscriber)

        testSubscriber.assertComplete()
        testSubscriber.assertValue(true)
    }

    @Test
    fun `has updated sets`() {
        val testSubscriber: TestObserver<Boolean> = TestObserver()

        repository.hasUpdatedSets.blockingSubscribe(testSubscriber)

        testSubscriber.assertComplete()
        testSubscriber.assertValue(true)
    }

    @Test
    fun `draw words enough times to drain set and force reload`() {
        val testSubscriber: TestObserver<Word> = TestObserver()

        repository.prepareWordsInstance({ "${Usage.DRAWING}_${Language.PL}" }, listOf("1"))
            .blockingAwait()

        repository.getWordsObservable()
            .doOnNext { println("Word: ${it.wordString}") }
            .subscribe(testSubscriber)

        repeat(8) {
            repository.requestWord { "${Usage.DRAWING}_${Language.PL}" }
        }

        testSubscriber.assertValueCount(8)
    }

}