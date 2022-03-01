package com.ick.kalambury.wordsrepository.model

class WordMatchingResult(
    val word: String,
    val answer: String,
    val isMatch: Boolean,
    val isCloseEnough: Boolean
)