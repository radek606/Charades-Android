package com.ick.kalambury.list.views

import android.content.Context
import android.util.AttributeSet
import com.ick.kalambury.databinding.ListItemMessageBinding
import com.ick.kalambury.list.model.SimpleData

class MessageListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ListItem<SimpleData, ListItemMessageBinding>(context, attrs, defStyleAttr, defStyleRes) {

    override val data: SimpleData
        get() = binding.data as SimpleData

}