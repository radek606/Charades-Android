package com.ick.kalambury.create

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.ick.kalambury.GameMode
import com.ick.kalambury.list.model.WordsSetData
import com.ick.kalambury.service.MockGameHandlerRepository
import com.ick.kalambury.settings.MockMainPreferenceStorage
import com.ick.kalambury.util.TestLogger
import com.ick.kalambury.util.TrampolineSchedulerProvider
import com.ick.kalambury.util.getOrAwaitValue
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.words.MockWordsInstanceDataSource
import com.ick.kalambury.words.MockWordsManifestDataSource
import com.ick.kalambury.words.MockWordsPropertiesStorage
import com.ick.kalambury.words.MockWordsSetDataSource
import com.ick.kalambury.wordsrepository.Language
import com.ick.kalambury.wordsrepository.WordsRepositoryImpl
import com.ick.kalambury.wordsrepository.model.WordsManifest
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.assertContentEquals

class CreateGameViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CreateGameViewModel

    @Before
    fun setUp() {
        Log.initialize(TestLogger())

        val savedStateHandle = SavedStateHandle(mapOf(
            "gameMode" to GameMode.SHOWING
        ))

        val repository = WordsRepositoryImpl(
            rootDirectory = { File("") },
            manifestDataSource = MockWordsManifestDataSource(getManifest()),
            wordsSetDataSource = MockWordsSetDataSource(),
            wordsInstanceDataSource = MockWordsInstanceDataSource(),
            wordsPropertiesStorage = MockWordsPropertiesStorage(1),
            scheduler = Schedulers.trampoline(),
            migrations = listOf()
        )

        viewModel = CreateGameViewModel(
            preferenceStorage = MockMainPreferenceStorage(wordsLanguage = Language.PL),
            wordsRepository = repository,
            gameHandlerRepository = MockGameHandlerRepository(),
            schedulers = TrampolineSchedulerProvider,
            stateHandle = savedStateHandle
        )
    }

    @Test
    fun `get available sets in language PL for game mode SHOWING and in correct order`() {
        val availableSets = viewModel.availableSets.getOrAwaitValue()

        //11 out of 17 sets meets conditions: language-pl, usage-showing
        assertEquals(availableSets.size, 11)

        //'Sport' set should be marked as new and be first on the list (primary comparator)
        assertEquals(availableSets[0].text, "Sport")
        assertTrue(availableSets[0].new)
        assertFalse(availableSets[0].updated)

        //'Idiomy' set should be marked as updated and be second on the list (secondary comparator)
        assertEquals(availableSets[1].text, "Idiomy")
        assertTrue(availableSets[1].updated)
        assertFalse(availableSets[1].new)

        //'Czynności' set should not be marked as updated nor new and be third on the list (tertiary comparator)
        assertEquals(availableSets[2].text, "Czynności")
        assertFalse(availableSets[2].updated)
        assertFalse(availableSets[2].new)
    }

    @Test
    fun `get selected sets based on default values from manifest`() {
        val availableSets = viewModel.selectedSets.getOrAwaitValue()

        assertEquals(availableSets.size, 9)
        assertContentEquals(
            availableSets.map(WordsSetData::id),
            listOf("a92b46b6", "c7cc1642", "5b6418a0", "e1faa8e5", "d152ae47", "88161761", "dc400574", "3bf27bfc", "822dd178")
        )
    }

    @Test
    fun `get selected sets based on user selection stored in data store`() {
        viewModel.onCategoriesSelected(listOf("c7cc1642", "88161761"))

        val availableSets = viewModel.selectedSets.getOrAwaitValue()

        assertEquals(availableSets.size, 2)
        assertContentEquals(
            availableSets.map(WordsSetData::id),
            listOf("c7cc1642", "88161761")
        )
    }

    private fun getManifest(): WordsManifest {
        return javaClass.classLoader?.getResourceAsStream("manifest.json")?.use {
            Json.decodeFromStream<WordsManifest>(it)
        } ?: throw FileNotFoundException()
    }
}