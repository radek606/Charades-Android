package com.ick.kalambury.list.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.ick.kalambury.BR

abstract class Connectable : BaseObservable() {

    @get:Bindable
    var connecting: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.connecting)
        }

}