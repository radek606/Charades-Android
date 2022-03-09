package com.ick.kalambury.wordsrepository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Usage {

    @SerialName("none")
    NONE,

    @SerialName("showing")
    SHOWING,

    @SerialName("drawing")
    DRAWING,

}