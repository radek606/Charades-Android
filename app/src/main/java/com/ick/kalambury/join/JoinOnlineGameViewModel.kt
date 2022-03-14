package com.ick.kalambury.join

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ick.kalambury.Event
import com.ick.kalambury.GameMode
import com.ick.kalambury.R
import com.ick.kalambury.TableKind
import com.ick.kalambury.join.JoinGameNavigationActions.NavigateToCreateGame
import com.ick.kalambury.list.model.TableData
import com.ick.kalambury.net.api.RestApiManager
import com.ick.kalambury.net.api.dto.TablesDto
import com.ick.kalambury.service.GameEvent
import com.ick.kalambury.service.GameEvent.State
import com.ick.kalambury.service.GameHandlerRepository
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

@HiltViewModel
class JoinOnlineGameViewModel @Inject constructor(
    private val apiManager: RestApiManager,
    gameHandlerRepository: GameHandlerRepository,
    private val schedulers: SchedulerProvider,
) : JoinGameViewModel<TableData>(gameHandlerRepository, schedulers) {

    override val gameMode = GameMode.DRAWING_ONLINE

    private val _tableDataMap: MutableLiveData<Map<TableKind, List<TableData>?>> = MutableLiveData()

    private fun fetchTables() {
        _swipeRefreshing.value = true
        disposables += apiManager.getTables()
            .observeOn(schedulers.main())
            .subscribe(::handleResult, ::handleError)
    }

    private fun handleResult(result: Result<TablesDto>) {
        result.fold(
            onSuccess = { tablesDto ->
                _swipeRefreshing.value = false
                _tableDataMap.value = tablesDto.tables.map { (kind, tableDto) ->
                    kind to tableDto.map { TableData(it) }.sorted()
                }.toMap()
            },
            onFailure = { handleError(it) }
        )
    }

    private fun handleError(error: Throwable) {
        Log.w(logTag, "Failed getting table data.", error)
        _swipeRefreshing.value = false
        _tableDataMap.value = mapOf()
        _snackbarMessage.value = Event(R.string.alert_nothing_found)
    }

    override fun getDataList(key: String?): LiveData<List<TableData>?> {
        return Transformations.map(_tableDataMap) { input: Map<TableKind, List<TableData>?> ->
            input[TableKind.valueOf(key!!)]
        }
    }

    override fun onRefresh() {
        fetchTables()
    }

    override fun handleGameEvent(event: GameEvent) {
        when (event.state) {
            State.KICKED -> handleConnectionError(R.string.snackbar_kicked)
            State.PLAYER_LIMIT_EXCEEDED -> handleConnectionError(R.string.snackbar_player_limit_exceeded)
            State.TABLE_NOT_FOUND -> handleConnectionError(R.string.snackbar_table_not_found)
            else -> { /* not applicable here */ }
        }
    }

    fun onCreate() {
        _navigationActions.value = Event(NavigateToCreateGame(GameMode.DRAWING_ONLINE))
    }

}