package com.ick.kalambury.service

import com.ick.kalambury.net.api.RestApiManager
import com.ick.kalambury.net.api.dto.TableConfigDto
import com.ick.kalambury.net.connection.User
import io.reactivex.rxjava3.core.Completable

/* "Host" variant of OnlineGameHandler existing just to perform createTable action
from create game flow before connecting to table. The rest of it's functions
are delegated to OnlineGameClientHandler */
class OnlineGameHostHandler(
    private val apiManager: RestApiManager,
    private val delegate: GameHandler
) : GameHandler by delegate {

    override fun connect(localUser: User, endpoint: Endpoint): Completable {
        return apiManager.createTable(
            localUser.uuid,
            localUser.nickname,
            TableConfigDto.fromGameConfig(delegate.config)
        ).flatMapCompletable { result ->
            result.fold(
                { delegate.connect(localUser, Endpoint(it.tableId, it.tableName)) },
                { Completable.error(it) }
            )
        }
    }

}