package com.ick.kalambury.list.views

import android.content.Context
import android.util.AttributeSet
import com.ick.kalambury.databinding.ListItemTableBinding
import com.ick.kalambury.list.model.TableData

class TableListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ListItem<TableData, ListItemTableBinding>(context, attrs, defStyleAttr, defStyleRes) {

    override val data: TableData
        get() = binding.data as TableData

}