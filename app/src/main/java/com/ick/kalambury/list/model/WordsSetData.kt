package com.ick.kalambury.list.model

import androidx.databinding.BaseObservable
import com.ick.kalambury.words.WordsSetInfo

data class WordsSetData(
        override val id: String,
        override val text: String,
        val desc: String,
        val new: Boolean,
        val updated: Boolean,
        override val selected: Boolean = false,
) : ListableData, BaseObservable() {

    constructor(info: WordsSetInfo, selected: Boolean = false) : this(
        info.id,
        info.name,
        info.description,
        info.isNew,
        info.isUpdated,
        selected
    )

}
