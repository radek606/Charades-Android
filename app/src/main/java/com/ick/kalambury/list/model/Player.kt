package com.ick.kalambury.list.model

import com.ick.kalambury.entities.GameDataProtos
import com.ick.kalambury.net.connection.ConnectionState
import com.ick.kalambury.net.connection.User
import kotlinx.parcelize.IgnoredOnParcel

data class Player(
    override val id: String,
    val user: User,
    var operator: Boolean = false,
    var winner: Boolean = false,
    var points: Int = 0,
    var roundsSinceLastDraw: Int = 0,
    var state: InGameState = InGameState.NONE,
    override val selected: Boolean = false,
) : ListableData, Comparable<Player> {

    enum class InGameState {
        NONE,
        INITIALIZING,
        IN_GAME,
        VOTING
    }

    @IgnoredOnParcel
    override val text: CharSequence
        get() = name

    @IgnoredOnParcel
    val name: String = user.nickname
    @IgnoredOnParcel
    val uuid: String = user.uuid
    @IgnoredOnParcel
    var connectionState: ConnectionState by user::connectionState

    val active: Boolean
        get() = state >= InGameState.IN_GAME

    constructor(user: User) : this(
        user.id,
        user,
        state = InGameState.INITIALIZING
    )

    fun updatePoints(point: Int) = points + point

    fun updateRoundsSinceLastDraw(rounds: Int) = roundsSinceLastDraw + rounds

    fun reset() {
        points = 0
        roundsSinceLastDraw = 0
        winner = false
    }

    fun toProto(): GameDataProtos.Player {
        return GameDataProtos.Player.newBuilder()
            .setId(uuid)
            .setNickname(name)
            .setPoints(points)
            .setActive(active)
            .setOperator(operator)
            .setWinner(winner)
            .build()
    }

    override fun compareTo(other: Player): Int {
        return if ((active && other.active) || (!this.active && !other.active)) {
            other.points.compareTo(points)
        } else {
            if (!active && other.active) 1 else -1
        }
    }

    fun clone(): Player {
        return copy(user = user.copy())
    }

    companion object {

        fun fromProto(proto: GameDataProtos.Player): Player {
            return Player(User(proto.id, proto.id, proto.nickname)).apply {
                connectionState = if(proto.active) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED
                state = if(proto.active) InGameState.IN_GAME else InGameState.NONE
                operator = proto.operator
                points = proto.points
                winner = proto.winner
            }
        }

    }

}
