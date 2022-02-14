package com.ick.kalambury.words

import com.ick.kalambury.GameMode
import com.ick.kalambury.list.model.WordsSetData
import com.ick.kalambury.util.TrampolineSchedulerProvider
import com.ick.kalambury.settings.MockMainPreferenceStorage
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Before
import org.junit.Test

class WordsRepositoryTest {

    private lateinit var repository: WordsRepository

    @Before
    fun init() {
        repository = WordsRepository(
            manifestDataSource = MockWordsManifestDataSource(),
            wordsSetDataSource = MockWordsSetDataSource(),
            wordsInstanceDataSource = MockWordsInstanceDataSource(),
            mainPreferenceStorage = MockMainPreferenceStorage(),
            schedulerProvider = TrampolineSchedulerProvider()
        )
    }

    @Test
    fun `load sets`() {
        val testSubscriber: TestObserver<List<WordsSetData>> = TestObserver()

        repository.getSetsData(GameMode.DRAWING_ONLINE, Language.PL)
            .blockingSubscribe(testSubscriber)

        testSubscriber.assertComplete()
        testSubscriber.assertValue { it.size == 2 }
    }

    @Test
    fun `draw words enough times to drain set and force reload`() {
        val testSubscriber: TestObserver<Word> = TestObserver()

        repository.prepareWordsInstance(GameMode.SHOWING, Language.PL, listOf("1"))
            .blockingAwait()

        repository.getWordsObservable()
            .doOnNext { println("Word: ${it.wordString}") }
            .subscribe(testSubscriber)

        repeat(8) {
            repository.requestWord(GameMode.SHOWING, Language.PL)
        }

        testSubscriber.assertValueCount(8)
    }

}