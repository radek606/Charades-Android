package com.ick.kalambury.wordsrepository

import com.ick.kalambury.wordsrepository.model.WordsManifest

data class WordsSetInfo internal constructor(
    val id: String,
    val name: String,
    val description: String,
    val language: Language = Language.EN,
    val new: Boolean,
    val updated: Boolean,
    var selected: Boolean = false
) {

    internal constructor(set: WordsManifest.Set, selected: Boolean) : this(
        set.id,
        set.name,
        set.description,
        set.language,
        set.isNew,
        set.isUpdated,
        selected
    )

}
