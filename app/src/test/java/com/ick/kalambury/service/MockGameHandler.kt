package com.ick.kalambury.service

import com.ick.kalambury.GameConfig
import com.ick.kalambury.net.connection.model.GameData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

class MockGameHandler : GameHandler {

    override var config: GameConfig = GameConfig()

    override fun ready(): Completable = Completable.complete()
    override fun connect(endpoint: Endpoint): Completable = Completable.complete()
    override fun handleLocalGameData(gameData: GameData) {}
    override fun getGameEvents(): Flowable<GameEvent> = Flowable.never()
    override fun finish() {}

}