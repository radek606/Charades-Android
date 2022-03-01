package com.ick.kalambury.service

import com.ick.kalambury.GameConfig
import com.ick.kalambury.GameMode

class MockGameHandlerRepository : GameHandlerRepository {

    override fun getHandler() = MockGameHandler()
    override fun createClientHandler(gameMode: GameMode) = MockGameHandler()
    override fun createHostHandler(config: GameConfig) = MockGameHandler()

}