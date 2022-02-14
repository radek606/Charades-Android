package com.ick.kalambury.list.model

import com.ick.kalambury.list.model.ListableData.Companion.DEFAULT_ID

data class SimpleData constructor(
        override val id: String = DEFAULT_ID,
        override val text: CharSequence,
        override val selected: Boolean = false,
) : ListableData, Connectable()
