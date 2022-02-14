package com.ick.kalambury.list.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.ick.kalambury.BR
import com.ick.kalambury.list.ListType
import com.ick.kalambury.list.model.ListableData

abstract class ListItem<D : ListableData, B : ViewDataBinding> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    lateinit var binding: B
    abstract val data: D

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding = DataBindingUtil.bind(this) ?: error("Binding failed!")
    }

    @Suppress("UNCHECKED_CAST")
    fun bind(itemMode: ListType.ItemMode, listableData: ListableData, selected: Boolean = false) {
        binding.apply {
            setVariable(BR.itemMode, itemMode)
            setVariable(BR.data, listableData as D)
            setVariable(BR.isChecked, selected)
            executePendingBindings()
        }
    }

}