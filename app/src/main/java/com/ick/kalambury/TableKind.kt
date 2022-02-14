package com.ick.kalambury

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class TableKind {
    DEFAULT, PUBLIC, PRIVATE
}