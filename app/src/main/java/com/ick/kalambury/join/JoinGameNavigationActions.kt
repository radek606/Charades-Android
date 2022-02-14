package com.ick.kalambury.join

import com.ick.kalambury.GameMode

sealed class JoinGameNavigationActions {
    object NavigateToGameFragment : JoinGameNavigationActions()
    class NavigateToCreateGame(val gameMode: GameMode) : JoinGameNavigationActions()
}