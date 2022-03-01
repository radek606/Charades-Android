package com.ick.kalambury.join

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ick.kalambury.TableKind
import com.ick.kalambury.di.NoopTrustStore
import com.ick.kalambury.net.DevHttpClientFactory
import com.ick.kalambury.net.api.RestApiManager
import com.ick.kalambury.service.MockGameHandlerRepository
import com.ick.kalambury.util.TrampolineSchedulerProvider
import com.ick.kalambury.util.getOrAwaitValue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class JoinOnlineGameViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockWebServer = MockWebServer()

    private lateinit var viewModel: JoinOnlineGameViewModel

    @Before
    fun setUp() {
        val client = DevHttpClientFactory.create(NoopTrustStore, null)
        val manager = RestApiManager(
            client,
            mockWebServer.url("/v2/game/tables/").toString(),
            TrampolineSchedulerProvider,
        )
        viewModel = JoinOnlineGameViewModel(
            manager,
            MockGameHandlerRepository(),
            TrampolineSchedulerProvider,
        )
    }

    @Test
    fun `fetch tables correctly sorted`() {
        mockWebServer.enqueueResponse("tables.json", 200)

        viewModel.onRefresh()

        val defaultTables = viewModel.getDataList(TableKind.DEFAULT.name).getOrAwaitValue()
        val customTables = viewModel.getDataList(TableKind.PUBLIC.name).getOrAwaitValue()

        assertNotNull(defaultTables)
        assertEquals(defaultTables!![0].id, "dev-11")

        assertNotNull(customTables)
        assertEquals(customTables!!.size ,1)
        assertNotNull(customTables[0].operator)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun MockWebServer.enqueueResponse(fileName: String, code: Int) {
        javaClass.classLoader?.getResourceAsStream(fileName)?.use {
            enqueue(
                MockResponse()
                    .setResponseCode(code)
                    .setBody(it.source().buffer().readUtf8())
            )
        }
    }

}