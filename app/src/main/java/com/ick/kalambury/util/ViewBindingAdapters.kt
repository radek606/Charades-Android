package com.ick.kalambury.util

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ick.kalambury.util.TimerMode.*
import java.util.*

@BindingAdapter("goneUnless")
fun goneUnless(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("swipeRefreshColors")
fun setSwipeRefreshColors(swipeRefreshLayout: SwipeRefreshLayout, colorResIds: IntArray) {
    swipeRefreshLayout.setColorSchemeColors(*colorResIds)
}

@BindingAdapter(value = ["timer", "timerMode"], requireAll = true)
fun setTimer(view: TextView, timer: Int, timerMode: TimerMode) {
    if (timerMode == GONE) {
        view.text = null
    } else {
        view.text = String.format(Locale.US, "%1d:%02d", timer / 60, timer % 60)
    }
    when (timerMode) {
        GONE,
        NORMAL -> view.setTextColor(Color.BLACK)
        WARN -> view.setTextColor(Color.RED)
    }
}

@BindingAdapter("label")
fun setLabel(view: TextView, data: Label?) {
    if (data == null) {
        view.text = null
        return
    }

    view.text = data.getText(view.context)
}