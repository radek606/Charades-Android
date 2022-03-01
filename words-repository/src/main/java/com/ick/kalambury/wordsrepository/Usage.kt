package com.ick.kalambury.wordsrepository

import com.fasterxml.jackson.annotation.JsonProperty

enum class Usage {

    @JsonProperty("none")
    NONE,

    @JsonProperty("showing")
    SHOWING,

    @JsonProperty("drawing")
    DRAWING,

}