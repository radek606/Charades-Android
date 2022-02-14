package com.ick.kalambury

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class PlayerChooseMethod {
    GUESSING_PLAYER, LONGEST_WAITING_PLAYER, RANDOM_PLAYER
}