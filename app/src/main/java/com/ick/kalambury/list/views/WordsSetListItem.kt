package com.ick.kalambury.list.views

import android.content.Context
import android.util.AttributeSet
import com.ick.kalambury.databinding.ListItemWordsSetBinding
import com.ick.kalambury.list.model.WordsSetData

class WordsSetListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ListItem<WordsSetData, ListItemWordsSetBinding>(context, attrs, defStyleAttr, defStyleRes) {

    override val data: WordsSetData
        get() = binding.data as WordsSetData

}