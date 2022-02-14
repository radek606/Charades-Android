package com.ick.kalambury.util

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.ick.kalambury.R
import java.util.*

class CustomNumberPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener {

    enum class PickedValueType {
        NUMBER, DURATION
    }

    interface OnValueChangeListener {
        fun onValueChanged(oldValue: Int, newValue: Int)
    }

    private lateinit var mTextValue: EditText
    private lateinit var pickedValueType: PickedValueType

    private var listener: OnValueChangeListener? = null
    private var _value = 0
    var value: Int
        get() = _value
        set(value) {
            _value = value
            updateValueText()
        }

    var stepValue = 0
    var maxValue = 0
    var minValue = 0

    init {
        initializeView(context)
        obtainValues(context, attrs)
    }

    private fun obtainValues(context: Context, attrs: AttributeSet?) {
        val values = context.obtainStyledAttributes(attrs, R.styleable.CustomNumberPicker)
        pickedValueType =
            PickedValueType.values()[values.getInt(R.styleable.CustomNumberPicker_picker_mode, 0)]
        _value = values.getInt(R.styleable.CustomNumberPicker_value, 0)
        minValue = values.getInt(R.styleable.CustomNumberPicker_min, Int.MIN_VALUE)
        maxValue = values.getInt(R.styleable.CustomNumberPicker_max, Int.MAX_VALUE)
        stepValue = values.getInt(R.styleable.CustomNumberPicker_step, 1)
        values.recycle()
    }

    private fun initializeView(context: Context) {
        context.inflate(R.layout.picker_layout, this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mTextValue = findViewById(R.id.picker_value)
        findViewById<View>(R.id.picker_add).setOnClickListener(this)
        findViewById<View>(R.id.picker_subtract).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val oldValue = _value

        val id = v.id
        if (id == R.id.picker_add) {
            if (_value < maxValue) {
                _value += stepValue
                listener?.onValueChanged(oldValue, _value)
            }
        } else if (id == R.id.picker_subtract) {
            if (_value > minValue) {
                _value -= stepValue
                listener?.onValueChanged(oldValue, _value)
            }
        }
        updateValueText()
    }

    private fun updateValueText() {
        when (pickedValueType) {
            PickedValueType.DURATION -> mTextValue.setText(String.format(Locale.US,
                "%01d:%02d",
                _value / 60,
                _value % 60))
            PickedValueType.NUMBER -> mTextValue.setText(_value.toString())
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superclass", super.onSaveInstanceState())
            putInt("type", pickedValueType.ordinal)
            putInt("value", value)
            putInt("min", minValue)
            putInt("max", maxValue)
            putInt("step", stepValue)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            state.apply {
                pickedValueType = PickedValueType.values()[getInt("type")]
                value = getInt("value")
                minValue = getInt("min")
                maxValue = getInt("max")
                stepValue = getInt("step")
            }
            super.onRestoreInstanceState(state.getParcelable("superclass"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        super.dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        super.dispatchThawSelfOnly(container)
    }

    fun setOnValueChangeListener(listener: OnValueChangeListener?) {
        this.listener = listener
    }

}