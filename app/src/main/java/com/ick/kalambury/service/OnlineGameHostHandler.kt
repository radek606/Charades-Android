package com.ick.kalambury.service

import com.ick.kalambury.net.api.RestApiManager
import com.ick.kalambury.net.api.dto.TableConfigDto
import com.ick.kalambury.settings.MainPreferenceStorage
import io.reactivex.rxjava3.core.Completable

/* "Host" variant of OnlineGameHandler existing just to perform createTable action
from create game flow before connecting to table. The rest of it's functions
are delegated to OnlineGameClientHandler */
class OnlineGameHostHandler(
    private val apiManager: RestApiManager,
    private val mainPreferenceStorage: MainPreferenceStorage,
    private val delegate: GameHandler
) : GameHandler by delegate {

    override fun connect(endpoint: Endpoint): Completable {
        return mainPreferenceStorage.localUserData
            .firstOrError()
            .flatMap {
                apiManager.createTable(
                    it.uuid,
                    it.nickname,
                    TableConfigDto.fromGameConfig(delegate.config)
                )
            }
            .flatMapCompletable { result ->
                result.fold(
                    { delegate.connect(Endpoint(it.tableId, it.tableName)) },
                    { Completable.error(it) }
                )
            }
    }

}