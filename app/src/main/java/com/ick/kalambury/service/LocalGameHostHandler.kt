package com.ick.kalambury.service

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Strategy
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.PlayerChooseMethod
import com.ick.kalambury.entities.ConnectionData
import com.ick.kalambury.entities.EndpointData
import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.list.model.Player
import com.ick.kalambury.net.connection.ConnectionState
import com.ick.kalambury.net.connection.User
import com.ick.kalambury.net.connection.model.ChatMessage
import com.ick.kalambury.net.connection.model.DrawableData
import com.ick.kalambury.net.connection.model.GameData
import com.ick.kalambury.net.connection.model.GameStateData
import com.ick.kalambury.service.nearbyconnections.NearbyConnectionsEvent
import com.ick.kalambury.service.nearbyconnections.RxHostNearbyConnections
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import com.ick.kalambury.words.InstanceId
import com.ick.kalambury.wordsrepository.WordMatcher
import com.ick.kalambury.wordsrepository.WordsRepository
import com.ick.kalambury.wordsrepository.model.Word
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.processors.PublishProcessor
import java.util.*
import java.util.concurrent.TimeUnit

class LocalGameHostHandler(
    conn: RxHostNearbyConnections,
    private val wordsRepository: WordsRepository,
) : HostGameHandler<RxHostNearbyConnections>(conn) {

    private lateinit var localPlayer: Player

    private lateinit var hostEndpointData: EndpointData

    private var gameState: GameState = GameState.NO_PLAYERS

    private val uiEvents: PublishProcessor<GameData> = PublishProcessor.create()

    private val users: MutableMap<String, User> = mutableMapOf()
    private var players: MutableMap<String, Player> = mutableMapOf()
    private var drawables: MutableList<DrawableData> = mutableListOf()
    private var firstPlayerId: String? = null
    private var drawingPlayerId: String? = null
    private var operatorPlayerId: String? = null
    private var winnerPlayerId: String? = null
    private var currentWord: Word? = null
    private var currentTimeLeft = 0
    private var inactivitySeconds = 0
    private var random: Random = Random()

    init {
        disposables += connection.connectionEvents()
            .observeOn(handlerThreadScheduler)
            .subscribe(::handleNearbyConnectionsEvents)

        disposables += uiEvents.onBackpressureBuffer()
            .observeOn(handlerThreadScheduler)
            .subscribe { handleGameData(localPlayer, it) }
    }

    //as host we don't need to connect to anything, so just do some initialization
    override fun connect(localUser: User, endpoint: Endpoint): Completable {
        return Completable.fromAction {
            initEndpointInfo(localUser)
            initLocalPlayer(localUser)
        }.subscribeOn(handlerThreadScheduler)
    }

    override fun ready(): Completable {
        return Completable.fromAction { initialState() }
            .subscribeOn(handlerThreadScheduler)
            .andThen(startAdvertising())
            .doOnError(::handleAdvertisingFailed)
            .onErrorComplete()
    }

    private fun initEndpointInfo(user: User) {
        hostEndpointData = EndpointData.newBuilder()
            .setId(user.nickname)
            .setName(user.nickname)
            .setMinVersionCode(BuildConfig.LOCAL_GAME_MIN_SUPPORTED_VERSION)
            .setMinVersionName(BuildConfig.LOCAL_GAME_MIN_SUPPORTED_VERSION_NAME)
            .build()
    }

    private fun initLocalPlayer(user: User) {
        localPlayer = Player(user).apply {
            connectionState = ConnectionState.CONNECTED
            state = Player.InGameState.IN_GAME
            operator = true
        }
        players[localPlayer.uuid] = localPlayer
    }

    private fun initialState() {
        firstPlayerId = localPlayer.uuid
        operatorPlayerId = localPlayer.uuid
        winnerPlayerId = null
        gameState = GameState.WAITING
        state = GameHandler.State.CONNECTED
        sendToUI(
            GameData.Builder()
                .withGameState(gameStateMessage)
                .addChatMessage(ChatMessage.waiting())
                .build()
        )
    }

    override fun finish() {
        if (state >= GameHandler.State.DISCONNECTED) {
            Log.w(logTag, "finish() - Already disconnected! Ignoring...")
            return
        }

        Log.d(logTag, "finish()")

        cancelGameTimer()
        state = GameHandler.State.DISCONNECTING
        disposables.dispose()

        connection.stopAdvertising()

        broadcastCompletable(GameData.action(GameData.QUIT_GAME), players.allExcept(localPlayer))
            .subscribeOn(handlerThreadScheduler)
            .onErrorComplete()
            .andThen {
                connection.disconnectAll()
                state = GameHandler.State.DISCONNECTED
                users.clear()
                players.clear()
                drawables.clear()
                it.onComplete()
            }
            .andThen(wordsRepository.saveWordsInstance(InstanceId(config.gameMode, config.language)))
            .doFinally { handlerThreadScheduler.shutdown() }
            .subscribeBy(onError = { Log.w(logTag, "Failed finishing game handler.", it) })
    }

    override fun handleLocalGameData(gameData: GameData) {
        uiEvents.onNext(gameData)
    }

    override fun handleRemoteMessageEvent(endpointId: String?, message: GameData) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleRemoteMessageEvent() - Already disconnecting or disconnected. Ignoring..."
            )
            return
        }

        val id = users[endpointId]?.uuid ?: error("Remote data from non existent player.")
        val player = players[id] ?: error("Remote data from non existent player.")

        Log.d(logTag, "handleRemoteMessageEvent(): $message from user: ${player.name}")

        handleGameData(player, message)
    }

    private fun handleGameData(player: Player, gameData: GameData) {
        when {
            gameData.hasAction(GameData.PLAYER_READY) -> {
                player.state = Player.InGameState.IN_GAME

                val builder = GameData.Builder()
                    .withPlayers(players)
                    .addChatMessage(ChatMessage.playerJoin(player.name))

                if (gameState == GameState.WAITING) {
                    drawingPlayerId = firstPlayerId
                    currentWord = nextWordToGuess
                    gameState = GameState.IN_GAME
                    drawables.clear()
                    builder.withGameState(gameStateMessage)
                    setGameTimer(config.roundTime)
                } else {
                    broadcast(
                        GameData.Builder()
                            .withPlayers(players)
                            .withGameState(gameStateMessage)
                            .withDrawables(drawables)
                            .build(), one(player)
                    )
                }
                send(builder.build(), players.all)
            }
            gameData.hasAction(GameData.ADD_NEW_OBJECT) -> {
                inactivitySeconds = 0
                drawables.addAll(gameData.drawables)
                send(gameData, players.allExcept(player))
            }
            gameData.hasAction(GameData.DELETE_LAST_OBJECT) -> {
                inactivitySeconds = 0
                drawables.removeLastOrNull()
                send(gameData, players.allExcept(player))
            }
            gameData.hasAction(GameData.CLEAR_SCREEN) -> {
                inactivitySeconds = 0
                drawables.clear()
                send(gameData, players.allExcept(player))
            }
            gameData.hasAction(GameData.CHAT_MESSAGE) -> {
                send(gameData, players.allExcept(player))
                handlePlayerAnswer(player, gameData)
            }
            gameData.hasAction(GameData.ABANDON_DRAWING) -> {
                player.updatePoints(-1)
                updateRounds()
                val lastPlayerId = drawingPlayerId
                drawingPlayerId = nextPlayerIdToDraw
                currentWord = nextWordToGuess
                drawables.clear()
                setGameTimer(config.roundTime)
                send(
                    GameData.Builder()
                        .withGameState(gameStateMessage)
                        .withPlayers(players)
                        .addChatMessage(ChatMessage.playerAbandon(players[lastPlayerId]!!.name))
                        .addChatMessage(ChatMessage.playerDraw(players[drawingPlayerId]!!.name))
                        .build(), players.all
                )
            }
            gameData.hasAction(GameData.CONTINUE) -> {
                gameState = GameState.IN_GAME
                resetPlayers()
                winnerPlayerId = null
                drawingPlayerId = operatorPlayerId
                currentWord = nextWordToGuess
                drawables.clear()
                setGameTimer(config.roundTime)
                send(
                    GameData.Builder()
                        .withGameState(gameStateMessage)
                        .withPlayers(players)
                        .addChatMessage(ChatMessage.playerDraw(players[drawingPlayerId]!!.name))
                        .build(), players.all
                )
            }
            else -> {
                Log.w(logTag, "handleGameData() - Ignored data: $gameData")
            }
        }
    }

    private fun handlePlayerAnswer(player: Player, message: GameData) {
        val answer = message.messages[0]
        if (answer.type == GameDataProtos.ChatMessage.Type.PLAYER_ANSWER) {
            val matchingResult = WordMatcher.matchAnswer(currentWord, answer.body)
            if (matchingResult.isMatch) {
                player.updatePoints(1)
                if (player.points == config.pointsLimit) {
                    setGameTimer(0)
                    player.winner = true
                    winnerPlayerId = player.uuid
                    gameState = GameState.FINISHED
                    send(
                        GameData.Builder()
                            .withPlayers(players)
                            .withGameState(gameStateMessage)
                            .addChatMessage(
                                ChatMessage.playerGuess(player.name, currentWord!!.wordString)
                            )
                            .addChatMessage(ChatMessage.playerWon(player.name))
                            .build(), players.all
                    )
                } else {
                    updateRounds()
                    val lastWord = currentWord!!.wordString
                    drawingPlayerId = getNextPlayerIdToDraw(player.uuid)
                    currentWord = nextWordToGuess
                    drawables.clear()
                    setGameTimer(config.roundTime)
                    send(
                        GameData.Builder()
                            .withPlayers(players)
                            .withGameState(gameStateMessage)
                            .addChatMessage(ChatMessage.playerGuess(player.name, lastWord))
                            .addChatMessage(ChatMessage.playerDraw(players[drawingPlayerId]!!.name))
                            .build(), players.all
                    )
                }
            } else if (matchingResult.isCloseEnough) {
                send(
                    GameData.Builder()
                        .addChatMessage(ChatMessage.closeEnoughAnswer(answer.body!!))
                        .build(), players.all
                )
            }
        }
    }

    private fun startAdvertising(): Completable {
        return connection.startAdvertising(
            hostEndpointData.toByteArray(),
            BuildConfig.LOCAL_GAME_SERVICE_ID,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        ).observeOn(handlerThreadScheduler)
    }

    private fun handleAdvertisingFailed(t: Throwable) {
        if (t is ApiException) {
            val code = t.statusCode
            if (code == ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING) {
                Log.w(logTag, "Called startAdvertising() but already advertising. Ignoring...")
                return
            }
        }

        Log.e(logTag, "Advertising start failed", t)

        sendToUI(GameEvent.State.ADVERTISING_FAILURE)
    }

    private fun handleNearbyConnectionsEvents(event: NearbyConnectionsEvent) {
        when (event) {
            is NearbyConnectionsEvent.ConnectionInitiated -> handleConnectionInitiatedEvent(event)
            is NearbyConnectionsEvent.ConnectionResult -> handleConnectionResultEvent(event)
            is NearbyConnectionsEvent.TransferUpdate -> handleMessageTransferUpdateEvent(event)
            is NearbyConnectionsEvent.Disconnected -> handleDisconnectedEvent(event)
            else -> {
                Log.w(logTag, "Got $event when in host mode. Ignoring...")
            }
        }
    }

    private fun handleConnectionInitiatedEvent(event: NearbyConnectionsEvent.ConnectionInitiated) {
        val (endpointId, connectionInfo) = event

        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleConnectionInitiatedEvent() - Already in $state state. Rejecting..."
            )
            connection.rejectConnection(endpointId).subscribe()
            return
        }

        val metadata: ConnectionData = try {
            ConnectionData.parseFrom(connectionInfo.endpointInfo)
        } catch (e: Exception) {
            Log.w(
                logTag,
                "handleConnectionInitiatedEvent() - invalid connection data. Rejecting...",
                e
            )
            connection.rejectConnection(endpointId).subscribe()
            return
        }

        val user = User(endpointId, metadata.uuid, metadata.nickname)
        if (metadata.version < BuildConfig.LOCAL_GAME_MIN_SUPPORTED_VERSION) {
            Log.w(
                logTag,
                "handleConnectionInitiatedEvent() - client version (${metadata.version}) " +
                        "lower than min supported version (${BuildConfig.LOCAL_GAME_MIN_SUPPORTED_VERSION}). Rejecting..."
            )
            connection.rejectConnection(endpointId).subscribe()
            return
        }

        if (metadata.version > BuildConfig.VERSION_CODE) {
            Log.w(
                logTag,
                "handleConnectionInitiatedEvent() - client version (${metadata.version}) " +
                        "higher than host (${BuildConfig.VERSION_CODE}). Rejecting..."
            )
            connection.rejectConnection(endpointId).subscribe()
            return
        }

        Log.d(
            logTag,
            "handleConnectionInitiatedEvent() - accepting connection with user: ${user.nickname}"
        )
        users[endpointId] = user
        connection.acceptConnection(endpointId).subscribe()
    }

    private fun handleConnectionResultEvent(event: NearbyConnectionsEvent.ConnectionResult) {
        val (endpointId, result) = event

        users[endpointId]?.let {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK,
                ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT,
                -> {
                    Log.d(
                        logTag, "handleConnectionResultEvent() with user: ${it.nickname}" +
                                ", status: ${result.status}"
                    )

                    handleNewRemotePlayer(it)
                }
                else -> {
                    Log.w(
                        logTag, "handleConnectionResultEvent() with user: ${it.nickname}" +
                                ", status: ${result.status}"
                    )

                    users.remove(it.uuid)
                    handleRemoteDisconnected(it)
                }
            }
        } ?: Log.w(
            logTag, "handleConnectionResultEvent() callback with unknown user" +
                    ", status: ${result.status}"
        )
    }

    private fun handleNewRemotePlayer(user: User) {
        val player = Player(user).apply {
            connectionState = ConnectionState.CONNECTED
        }
        players[user.uuid] = player

        broadcast(GameData.config(config), one(player))
    }

    private fun handleRemoteDisconnected(user: User) {
        val player = players.computeIfPresent(user.uuid) { _, p ->
            p.apply {
                state = Player.InGameState.NONE
                connectionState = ConnectionState.DISCONNECTED
            }
        } ?: return

        when {
            activePlayersCount >= 2 -> {
                val builder = GameData.Builder()
                    .withPlayers(players)
                    .addChatMessage(ChatMessage.playerLeft(player.name))
                if (gameState == GameState.IN_GAME && drawingPlayerId == player.uuid) {
                    updateRounds()
                    drawingPlayerId = nextPlayerIdToDraw
                    currentWord = nextWordToGuess
                    drawables.clear()
                    builder.withGameState(gameStateMessage)
                    builder.addChatMessage(ChatMessage.playerDraw(players[drawingPlayerId]!!.name))
                    setGameTimer(config.roundTime)
                }
                send(builder.build(), players.all)
            }
            activePlayersCount == 1 -> {
                gameState = GameState.WAITING
                gameTimer?.cancel()
                drawables.clear()
                players.clear()
                players[localPlayer.uuid] = localPlayer
                localPlayer.reset()
                drawingPlayerId = null
                sendToUI(
                    GameData.Builder()
                        .withGameState(gameStateMessage)
                        .addChatMessage(ChatMessage.playerLeft(player.name))
                        .addChatMessage(ChatMessage.waiting())
                        .build()
                )
            }
        }
    }

    private fun handleMessageTransferUpdateEvent(event: NearbyConnectionsEvent.TransferUpdate) {
        val (endpointId, update) = event

        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleMessageTransferUpdateEvent() - Already in $state state. Ignoring..."
            )
            return
        }

        val user = users[endpointId]
        if (user != null) {
            Log.v(
                logTag, "handleMessageTransferUpdateEvent() from user: " +
                        "${user.nickname}, msgId: ${update.payloadId}, " +
                        "status: ${update.status}, bytes: ${update.bytesTransferred}"
            )
        } else {
            Log.w(
                logTag, "handleMessageTransferUpdateEvent() from unknown endpoint: " +
                        "$endpointId, msgId: ${update.payloadId}, " +
                        "status: ${update.status}, bytes: ${update.bytesTransferred}"
            )
        }
    }

    private fun handleDisconnectedEvent(event: NearbyConnectionsEvent.Disconnected) {
        if (state >= GameHandler.State.DISCONNECTING) {
            Log.w(
                logTag,
                "handleDisconnectedEvent() - Already in $state state. Ignoring..."
            )
            return
        }

        val user = users.remove(event.endpointId)
        if (user != null) {
            Log.d(logTag, "handleDisconnectedEvent() with user: ${user.nickname}")
            handleRemoteDisconnected(user)
        } else {
            Log.w(logTag, "handleDisconnectedEvent() callback with unknown user!")
        }
    }

    private fun getNextPlayerIdToDraw(guessingPlayerId: String): String {
        inactivitySeconds = 0
        return if (config.playerChooseMethod === PlayerChooseMethod.GUESSING_PLAYER) {
            guessingPlayerId
        } else {
            nextPlayerIdToDraw
        }
    }

    private val nextPlayerIdToDraw: String
        get() {
            val tempPlayers = activePlayersList.filter { p -> p.uuid != drawingPlayerId }
            inactivitySeconds = 0
            return when (config.playerChooseMethod) {
                PlayerChooseMethod.RANDOM_PLAYER ->
                    tempPlayers[random.nextInt(tempPlayers.size)].uuid
                PlayerChooseMethod.GUESSING_PLAYER,
                PlayerChooseMethod.LONGEST_WAITING_PLAYER,
                ->
                    tempPlayers.maxByOrNull { p -> p.roundsSinceLastDraw }!!.uuid
            }
        }

    private val activePlayersList: List<Player>
        get() = players.values.filter(Player::active)

    private val activePlayersCount: Int
        get() = activePlayersList.count()

    private fun updateRounds() {
        players.values.forEach { p -> p.roundsSinceLastDraw++ }
        players[drawingPlayerId]?.roundsSinceLastDraw = 0
    }

    private fun resetPlayers() {
        val it: MutableIterator<Map.Entry<String?, Player>> = players.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (!entry.value.active) {
                it.remove()
            } else {
                entry.value.reset()
            }
        }
    }

    private val nextWordToGuess: Word
        get() = wordsRepository.getWord(InstanceId(config.gameMode, config.language)).blockingGet()

    private val gameStateMessage: GameStateData
        get() = GameStateData.newBuilder(gameState)
            .setDrawingPlayerId(drawingPlayerId)
            .setOperatorPlayerId(operatorPlayerId)
            .setWinnerPlayerId(winnerPlayerId)
            .setWordToGuess(currentWord?.wordString)
            .setCategory(currentWord?.setName)
            .setTimeLeft(currentTimeLeft)
            .build()

    override fun setGameTimer(seconds: Int) {
        inactivitySeconds = 0
        gameTimer?.cancel()

        if (seconds <= 0) {
            return
        }

        gameTimer = object :
            RxCountDownTimer(seconds.toLong(), 1, TimeUnit.SECONDS, handlerThreadScheduler) {
            override fun onTick(tick: Long) {
                currentTimeLeft = tick.toInt()
                inactivitySeconds++
                if (currentTimeLeft == seconds / 2) {
                    val data = GameData.Builder()
                        .addChatMessage(ChatMessage.hint(currentWord!!.wordString.substring(0, 1)))
                        .build()
                    send(data, players.all)
                }
                if (currentTimeLeft == seconds / 4) {
                    val data = GameData.Builder()
                        .addChatMessage(ChatMessage.hint(currentWord!!.wordString.substring(0, 2)))
                        .build()
                    send(data, players.all)
                }
                if (currentTimeLeft == seconds / 8) {
                    val data = GameData.Builder()
                        .addChatMessage(ChatMessage.littleTimeWarn())
                        .build()
                    send(data, one(players[drawingPlayerId]!!))
                }
                if (inactivitySeconds == INACTIVITY_LIMIT_SECONDS / 3 * 2) {
                    val data = GameData.Builder()
                        .addChatMessage(ChatMessage.inactivityWarn())
                        .build()
                    send(data, one(players[drawingPlayerId]!!))
                }
                if (inactivitySeconds >= INACTIVITY_LIMIT_SECONDS) {
                    updateRounds()
                    val lastPlayer = drawingPlayerId
                    drawingPlayerId = nextPlayerIdToDraw
                    currentWord = nextWordToGuess
                    drawables.clear()
                    setGameTimer(config.roundTime)
                    val data = GameData.Builder()
                        .withGameState(gameStateMessage)
                        .addChatMessage(ChatMessage.playerInactive(players[lastPlayer]!!.name))
                        .addChatMessage(ChatMessage.playerDraw(players[drawingPlayerId]!!.name))
                        .build()
                    send(data, players.all)
                }

                sendToUI(GameData.action(GameData.TIMER, currentTimeLeft.toString()))
            }

            override fun onFinish() {
                updateRounds()
                val lastWord = currentWord!!.wordString
                drawingPlayerId = nextPlayerIdToDraw
                currentWord = nextWordToGuess
                drawables.clear()
                setGameTimer(config.roundTime)
                val data = GameData.Builder()
                    .withGameState(gameStateMessage)
                    .withChatMessages(
                        ChatMessage.timeIsUp(),
                        ChatMessage.word(lastWord),
                        ChatMessage.playerDraw(players[drawingPlayerId]!!.name)
                    )
                    .build()
                send(data, players.all)
            }
        }.start()
    }

    private fun send(message: GameData, ids: List<String>) {
        val tmp = ids.toMutableList()
        val containsLocalPlayerId = tmp.remove(localPlayer.id)
        if (tmp.isNotEmpty()) {
            broadcast(message, tmp)
        }
        if (containsLocalPlayerId) {
            sendToUI(message)
        }
    }

    companion object {
        private const val INACTIVITY_LIMIT_SECONDS = 60
    }

}