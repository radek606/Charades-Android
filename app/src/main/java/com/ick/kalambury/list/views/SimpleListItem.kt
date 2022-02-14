package com.ick.kalambury.list.views

import android.content.Context
import android.util.AttributeSet
import com.ick.kalambury.databinding.ListItemSimpleBinding
import com.ick.kalambury.list.model.SimpleData

class SimpleListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ListItem<SimpleData, ListItemSimpleBinding>(context, attrs, defStyleAttr, defStyleRes) {

    override val data: SimpleData
        get() = binding.data as SimpleData

}