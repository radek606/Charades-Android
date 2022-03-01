package com.ick.kalambury.join

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.Event
import com.ick.kalambury.GameMode
import com.ick.kalambury.R
import com.ick.kalambury.list.model.EndpointData
import com.ick.kalambury.net.connection.SupportedVersionInfo
import com.ick.kalambury.service.GameEvent
import com.ick.kalambury.service.GameHandlerRepository
import com.ick.kalambury.util.SchedulerProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class JoinLocalGameViewModel @Inject constructor(
    gameHandlerRepository: GameHandlerRepository,
    schedulerProvider: SchedulerProvider,
): JoinGameViewModel<EndpointData>(gameHandlerRepository, schedulerProvider) {

    override val gameMode = GameMode.DRAWING_LOCAL

    private val _hostsList: MutableLiveData<List<EndpointData>?> = MutableLiveData()

    override fun getDataList(key: String?): LiveData<List<EndpointData>?> {
        return _hostsList
    }

    override fun onRefresh() {
        _swipeRefreshing.value = true

        _hostsList.value = listOf()

        gameHandler.startDiscovery(5000L)
    }

    override fun onItemClicked(item: EndpointData) {
        if (item.minVersionCode > BuildConfig.VERSION_CODE) {
            handleUnsupportedVersion(SupportedVersionInfo(item.minVersionCode, item.minVersionName))
        } else {
            super.onItemClicked(item)
        }
    }

    override fun handleGameEvent(event: GameEvent) {
        when (event.state) {
            GameEvent.State.DISCOVERING -> _hostsList.value = event.hostEndpoints
            GameEvent.State.DISCOVERY_FINISHED -> handleDiscoveryFinished()
            GameEvent.State.DISCOVERY_FAILURE -> handleConnectionError(R.string.alert_discovery_failed_message, false)
            else -> { /* not applicable here */ }
        }
    }

    private fun handleDiscoveryFinished() {
        _swipeRefreshing.value = false
        if (_hostsList.value == null || _hostsList.value!!.isEmpty()) {
            _snackbarMessage.value = Event(R.string.alert_nothing_found)
        }
    }

}