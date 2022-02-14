package com.ick.kalambury

import androidx.annotation.Keep

@Keep
enum class GameMode(val wordsSetType: String) {

    NONE(""),
    SHOWING("showing"),
    DRAWING_LOCAL("drawing"),
    DRAWING_ONLINE("drawing");

}