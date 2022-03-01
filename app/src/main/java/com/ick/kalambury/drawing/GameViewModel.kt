package com.ick.kalambury.drawing

import android.text.Spanned
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.parseAsHtml
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ick.kalambury.BaseViewModel
import com.ick.kalambury.Event
import com.ick.kalambury.GameMode
import com.ick.kalambury.R
import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.list.model.Player
import com.ick.kalambury.list.model.SimpleData
import com.ick.kalambury.net.connection.User
import com.ick.kalambury.net.connection.model.ChatMessage
import com.ick.kalambury.net.connection.model.DrawableData
import com.ick.kalambury.net.connection.model.GameData
import com.ick.kalambury.net.connection.model.GameStateData
import com.ick.kalambury.service.GameEvent
import com.ick.kalambury.service.GameHandler
import com.ick.kalambury.service.GameHandlerRepository
import com.ick.kalambury.service.GameState
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.Label
import com.ick.kalambury.util.LimitedMutableList
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.util.TimerMode
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.wordsrepository.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    gameHandlerRepository: GameHandlerRepository,
    private val preferenceStorage: MainPreferenceStorage,
    private val messageFormatter: MessageFormatter,
    private val schedulerProvider: SchedulerProvider,
) : BaseViewModel<Unit>() {

    enum class ViewTransitions {
        DRAWING_EXPANDED_TO_DRAWING_COLLAPSED,
        DRAWING_COLLAPSED_TO_DRAWING_EXPANDED,
        DRAWING_EXPANDED_TO_GUESSING_EXPANDED,
        GUESSING_EXPANDED_TO_DRAWING_EXPANDED,
        DRAWING_COLLAPSED_TO_GUESSING_COLLAPSED,
        GUESSING_COLLAPSED_TO_DRAWING_COLLAPSED,
        GUESSING_EXPANDED_TO_GUESSING_COLLAPSED,
        GUESSING_COLLAPSED_TO_GUESSING_EXPANDED,
    }

    private val gameHandler: GameHandler = gameHandlerRepository.getHandler()
    private val messages: MutableList<SimpleData> = mutableListOf()
    private lateinit var lastMessages: LimitedMutableList<String>

    lateinit var self: User

    val gameMode: GameMode = gameHandler.config.gameMode
    val isHost: Boolean = gameHandler.config.isHost
    val language: Language = gameHandler.config.language

    var gameState: GameStateData? = null
    var players: Map<String, Player> = mapOf()

    val isOperator: Boolean
        get() = self.uuid == gameState?.operatorPlayerId

    private val _isDrawing: MutableLiveData<Boolean> = MutableLiveData()
    val isDrawing: Boolean
        get() = _isDrawing.value == true

    private val _isFullChatOpened: MutableLiveData<Boolean> = MutableLiveData()
    val isFullChatOpened: Boolean
        get() = _isFullChatOpened.value == true

    private val _timer: MutableLiveData<Int> = MutableLiveData(0)
    val timer: LiveData<Int> = _timer

    private val _timerMode: MutableLiveData<TimerMode> = MutableLiveData(TimerMode.GONE)
    val timerMode: LiveData<TimerMode> = _timerMode

    private val _fullChatMessages: MutableLiveData<List<SimpleData>> = MutableLiveData()
    val fullChatMessages: LiveData<List<SimpleData>> = _fullChatMessages

    private val _smallChatMessages: MutableLiveData<Spanned> = MutableLiveData()
    val smallChatMessages: LiveData<Spanned> = _smallChatMessages

    private val _drawEvents: MutableLiveData<DrawEvent> = MutableLiveData()
    val drawEvents: LiveData<DrawEvent> = _drawEvents

    private val _leftLabel: MutableLiveData<Label?> = MutableLiveData()
    val leftLabel: LiveData<Label?> = _leftLabel

    private val _rightLabel: MutableLiveData<Label?> = MutableLiveData()
    val rightLabel: LiveData<Label?> = _rightLabel

    private val _finishEvent: MutableLiveData<Event<FinishEvent>> = MutableLiveData()
    val finishEvent: LiveData<Event<FinishEvent>> = _finishEvent

    private val _finishedGameData: MutableLiveData<Event<FinishedGameData>> = MutableLiveData()
    val finishedGameData: LiveData<Event<FinishedGameData>> = _finishedGameData

    private val _viewTransitions = MediatorLiveData<ViewTransitions>().apply {
        addSource(Transformations.distinctUntilChanged(_isDrawing)) { drawing ->
            value = if (drawing) {
                if (true == _isFullChatOpened.value) {
                    ViewTransitions.GUESSING_EXPANDED_TO_DRAWING_EXPANDED
                } else {
                    ViewTransitions.GUESSING_COLLAPSED_TO_DRAWING_COLLAPSED
                }
            } else {
                if (true == _isFullChatOpened.value) {
                    ViewTransitions.DRAWING_EXPANDED_TO_GUESSING_EXPANDED
                } else {
                    ViewTransitions.DRAWING_COLLAPSED_TO_GUESSING_COLLAPSED
                }
            }
        }
        addSource(Transformations.distinctUntilChanged(_isFullChatOpened)) { opened ->
            value = if (opened) {
                if (true == _isDrawing.value) {
                    ViewTransitions.DRAWING_COLLAPSED_TO_DRAWING_EXPANDED
                } else {
                    ViewTransitions.GUESSING_COLLAPSED_TO_GUESSING_EXPANDED
                }
            } else {
                if (true == _isDrawing.value) {
                    ViewTransitions.DRAWING_EXPANDED_TO_DRAWING_COLLAPSED
                } else {
                    ViewTransitions.GUESSING_EXPANDED_TO_GUESSING_COLLAPSED
                }
            }
        }
    }
    val viewTransitions: LiveData<ViewTransitions> = _viewTransitions

    init {
        disposables += gameHandler.getGameEvents()
            .observeOn(schedulerProvider.main())
            .subscribe(::handleGameEvents)

        disposables += preferenceStorage.localUserData
            .firstOrError()
            .doAfterSuccess { self = it }
            .flatMap { preferenceStorage.chatSize.firstOrError() }
            .doAfterSuccess { lastMessages = LimitedMutableList(it) }
            .flatMapCompletable { gameHandler.ready() }
            .observeOn(schedulerProvider.main())
            .subscribe()

        val config = gameHandler.config
        if (config.name.isNullOrEmpty()) {
            _rightLabel.value = Label.res(R.string.ga_points_limit, false, config.pointsLimit)
        } else {
            _rightLabel.value = Label.res(R.string.ga_table_name_and_points, true, config.name!!, config.pointsLimit)
        }
    }

    private fun handleGameEvents(event: GameEvent) {
        when(event.state) {
            GameEvent.State.CONNECTED -> handleGameData(event.gameData)
            GameEvent.State.HOST_FINISHED -> {
                preferenceStorage.setLastGameWithoutError(true)
                _finishEvent.value = Event(FinishEvent.Remote(R.string.alert_host_terminated))
            }
            GameEvent.State.ADVERTISING_FAILURE -> {
                preferenceStorage.setLastGameWithoutError(false)
                _finishEvent.value = Event(FinishEvent.Error(R.string.alert_advertising_failed))
            }
            GameEvent.State.KICKED -> {
                preferenceStorage.setLastGameWithoutError(true)
                _finishEvent.value = Event(FinishEvent.Remote(R.string.alert_kicked))
            }
            GameEvent.State.NETWORK_FAILURE,
            GameEvent.State.GENERIC_FAILURE -> {
                preferenceStorage.setLastGameWithoutError(false)
                _finishEvent.value = Event(FinishEvent.Error(R.string.alert_connection_lost))
            }
            else -> { /* not applicable here */ }
        }
    }

    private fun handleGameData(data: GameData?) {
        if (data == null) {
            Log.w(logTag(), "Received event from service but GameData == null")
            return
        }

        Log.v(logTag(), "Game data from service: $data")

        if (data.hasAction(GameData.PLAYER_UPDATE)) {
            players = data.players
        }
        if (data.hasAction(GameData.GAME_STATE_CHANGE)) {
            handleGameStateChange(data.gameStateData ?: throw IllegalArgumentException())
        }
        if (data.hasAction(GameData.TIMER)) {
            _timer.value = data.actionData?.toInt()
        }
        if (data.hasAction(GameData.ADD_NEW_OBJECT)) {
            _drawEvents.value = DrawEvent.Add(data.drawables)
        }
        if (data.hasAction(GameData.DELETE_LAST_OBJECT)) {
            _drawEvents.value = DrawEvent.Undo
        }
        if (data.hasAction(GameData.CLEAR_SCREEN)) {
            _drawEvents.value = DrawEvent.Clear
        }
        if (data.hasAction(GameData.CHAT_MESSAGE)) {
            handleChatMessages(data.messages)
            if (data.getMessage(GameDataProtos.ChatMessage.Type.LITTLE_TIME_WARN) != null) {
                _timerMode.value = TimerMode.WARN
            }
        }
        if (data.hasAction(GameData.GAME_FINISH)) {
            val playersCopy = players.values.map { it.clone() }
            _finishedGameData.value = Event(FinishedGameData(playersCopy, players[data.gameStateData!!.winnerPlayerId]!!.name))
            _finishEvent.value = Event(FinishEvent.RemoteFinish)
        }
    }

    private fun handleGameStateChange(data: GameStateData) {
        gameState = data

        _drawEvents.value = DrawEvent.Clear
        _leftLabel.value = null

        if (data.state == GameState.WAITING) {
            _isDrawing.value = true
            _timerMode.value = TimerMode.GONE
        } else if (data.state == GameState.IN_GAME) {
            val drawing = self.uuid == data.drawingPlayerId
            _isDrawing.value = drawing
            if (drawing) {
                _timerMode.value = TimerMode.NORMAL
                _leftLabel.value = Label.res(R.string.da_password, true, data.wordToGuess!!, data.category!!)
            } else {
                _timerMode.value = TimerMode.GONE
                _leftLabel.value = Label.res(R.string.ga_drawing_player, true, players[data.drawingPlayerId]?.name!!)
            }
        }
    }

    private fun handleChatMessages(msgList: List<ChatMessage>) {
        msgList.forEach {
            val formatted = messageFormatter.format(it)
            messages.add(SimpleData(text = formatted.parseAsHtml(FROM_HTML_MODE_COMPACT)))
            lastMessages.add(formatted)
        }

        _fullChatMessages.value = messages

        val smallChatString = lastMessages.joinToString(separator = "<br />")
        _smallChatMessages.value = smallChatString.parseAsHtml(FROM_HTML_MODE_COMPACT)
    }

    fun onDraw(data: DrawableData) {
        gameHandler.handleLocalGameData(GameData.drawable(data))
    }

    fun onDelete() {
        _drawEvents.value = DrawEvent.Undo
        gameHandler.handleLocalGameData(GameData.action(GameData.DELETE_LAST_OBJECT))
    }

    fun onClear() {
        _drawEvents.value = DrawEvent.Clear
        gameHandler.handleLocalGameData(GameData.action(GameData.CLEAR_SCREEN))
    }

    fun onAbandonDrawing() {
        gameHandler.handleLocalGameData(GameData.action(GameData.ABANDON_DRAWING))
    }

    fun onFullChatOpened() {
        _isFullChatOpened.value = true
    }

    fun onFullChatClosed() {
        _isFullChatOpened.value = false
    }

    fun onLocalChatMessage(text: String, isAnswer: Boolean) {
        if (text.isNotEmpty()) {
            val message: ChatMessage = if (isAnswer) {
                ChatMessage.playerAnswer(self.nickname, text)
            } else {
                ChatMessage.playerWrite(self.nickname, text)
            }

            handleChatMessages(listOf(message))

            gameHandler.handleLocalGameData(GameData.message(message))
        }
    }

    fun onKickPlayers(playerIds: List<String>) {
        playerIds.forEach {
            gameHandler.handleLocalGameData(GameData.action(GameData.KICK_PLAYER, it))
        }
    }

    fun onLeave() {
        _finishEvent.value = Event(FinishEvent.LocalLeave)
    }

    fun onFinish() {
        gameHandler.finish()
    }

    data class FinishedGameData(val players: List<Player>, val winnerName: String)

    sealed class FinishEvent(val showAd: Boolean) {
        object RemoteFinish : FinishEvent(true)
        object LocalLeave : FinishEvent(true)
        open class Remote(@StringRes val messageId: Int, showAd: Boolean = true) : FinishEvent(showAd)
        class Error(@StringRes messageId: Int, showAd: Boolean = false) : Remote(messageId, showAd)
    }

    sealed class DrawEvent {
        class Add(val drawables: List<DrawableData>) : DrawEvent()
        object Undo : DrawEvent()
        object Clear : DrawEvent()
    }

}