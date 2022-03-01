package com.ick.kalambury.join

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ick.kalambury.*
import com.ick.kalambury.list.model.Connectable
import com.ick.kalambury.list.model.ListableData
import com.ick.kalambury.net.connection.SupportedVersionInfo
import com.ick.kalambury.service.ClientGameHandler
import com.ick.kalambury.service.Endpoint
import com.ick.kalambury.service.GameEvent
import com.ick.kalambury.service.GameHandlerRepository
import com.ick.kalambury.util.SchedulerProvider
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy

abstract class JoinGameViewModel<D : ListableData> constructor(
    private val gameHandlerRepository: GameHandlerRepository,
    private val schedulerProvider: SchedulerProvider,
) : BaseViewModel<JoinGameNavigationActions>() {

    abstract val gameMode: GameMode

    protected lateinit var gameHandler: ClientGameHandler<*>

    protected val _incompatibleVersion: MutableLiveData<Event<SupportedVersionInfo>> =
        MutableLiveData()
    val incompatibleVersion: LiveData<Event<SupportedVersionInfo>> = _incompatibleVersion

    var gameEventsDisposable: Disposable? = null

    private var selectedItem: Connectable? = null

    fun onStart() {
        createGameHandler()

        selectedItem?.connecting = false

        onRefresh()
    }

    private fun createGameHandler() {
        gameHandler = gameHandlerRepository.createClientHandler(gameMode) as ClientGameHandler<*>

        gameEventsDisposable?.dispose()
        gameEventsDisposable = gameHandler.getGameEvents()
            .observeOn(schedulerProvider.main())
            .subscribe(::handleCommonGameEvent)
    }

    open fun onItemClicked(item: D) {
        if (selectedItem != null && selectedItem!!.connecting) return

        selectedItem = item as? Connectable
        selectedItem?.connecting = true

        disposables += gameHandler.connect(Endpoint(item.id, item.text.toString()))
            .observeOn(schedulerProvider.main())
            .subscribeBy(
                onComplete = {
                    /* onComplete do nothing, it's just confirmation that connection process
                    started without error, proper result will come as GameEventData from GameEvents flowable */
                },
                onError = {
                    selectedItem?.connecting = false
                    _snackbarMessage.value = Event(R.string.alert_no_internet)
                }
            )
    }

    private fun handleCommonGameEvent(event: GameEvent) {
        when (event.state) {
            GameEvent.State.CONNECTED -> handleConnected(event.config)
            GameEvent.State.UNSUPPORTED_VERSION -> handleUnsupportedVersion(event.supportedVersionInfo)
            GameEvent.State.NETWORK_FAILURE -> handleConnectionError(
                R.string.snackbar_connection_fail,
                false
            )
            else -> handleGameEvent(event)
        }
    }

    private fun handleConnected(config: GameConfig?) {
        if (config == null) error("No game config!")

        gameEventsDisposable?.dispose()
        gameEventsDisposable = null

        gameHandler.config = GameConfig(
            gameMode = gameMode,
            isHost = false,
            roundTime = config.roundTime,
            pointsLimit = config.pointsLimit,
            playerChooseMethod = config.playerChooseMethod,
            language = config.language,
            name = config.name
        )
        _navigationActions.value = Event(JoinGameNavigationActions.NavigateToGameFragment)
    }

    fun handleUnsupportedVersion(info: SupportedVersionInfo?) {
        selectedItem?.connecting = false
        _swipeRefreshing.value = false
        _incompatibleVersion.value = Event(info ?: SupportedVersionInfo())
    }

    fun handleConnectionError(@StringRes msgId: Int, refresh: Boolean = true) {
        selectedItem?.connecting = false
        _swipeRefreshing.value = false
        _snackbarMessage.value = Event(msgId)
        createGameHandler()
        if (refresh) {
            onRefresh()
        }
    }

    override fun onCleared() {
        super.onCleared()

        gameEventsDisposable?.dispose()
    }

    abstract fun getDataList(key: String?): LiveData<List<D>?>

    abstract fun onRefresh()
    abstract fun handleGameEvent(event: GameEvent)

}