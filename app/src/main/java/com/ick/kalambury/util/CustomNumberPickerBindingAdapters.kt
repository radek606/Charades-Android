package com.ick.kalambury.util

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

@BindingAdapter("value")
fun setValue(picker: CustomNumberPicker, newValue: Int) {
    if (picker.value != newValue) {
        picker.value = newValue
    }
}

@InverseBindingAdapter(attribute = "value")
fun getValue(picker: CustomNumberPicker): Int {
    return picker.value
}

@BindingAdapter("valueAttrChanged")
fun setOnValueChangedListener(picker: CustomNumberPicker, attrChange: InverseBindingListener) {
    picker.setOnValueChangeListener(object : CustomNumberPicker.OnValueChangeListener {
        override fun onValueChanged(oldValue: Int, newValue: Int) {
            attrChange.onChange()
        }
    })
}