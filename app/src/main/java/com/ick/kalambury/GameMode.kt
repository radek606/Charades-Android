package com.ick.kalambury

import androidx.annotation.Keep
import com.ick.kalambury.wordsrepository.Usage

@Keep
enum class GameMode(val wordsSetUsage: Usage) {

    NONE(Usage.NONE),
    SHOWING(Usage.SHOWING),
    DRAWING_LOCAL(Usage.DRAWING),
    DRAWING_ONLINE(Usage.DRAWING);

    override fun toString(): String {
        return name
    }

}