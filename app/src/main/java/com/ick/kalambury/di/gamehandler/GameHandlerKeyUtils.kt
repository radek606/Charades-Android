package com.ick.kalambury.di.gamehandler

import com.google.auto.value.AutoAnnotation
import com.ick.kalambury.GameMode

object GameHandlerKeyUtils {

    @JvmStatic
    @AutoAnnotation
    fun create(mode: GameMode?, host: Boolean): GameHandlerKey {
        return AutoAnnotation_GameHandlerKeyUtils_create(mode, host)
    }

}