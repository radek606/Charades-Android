package com.ick.kalambury.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.GridView
import android.widget.ListView
import android.widget.PopupWindow
import com.ick.kalambury.R
import com.ick.kalambury.drawing.adapters.ColorPaletteAdapter
import com.ick.kalambury.drawing.adapters.LineWeightAdapter
import com.ick.kalambury.util.inflate
import com.ick.kalambury.util.toPx

object PopupWindows {

    @SuppressLint("InflateParams")
    fun createColorPalettePopup(
        context: Context,
        callback: (Int) -> Unit,
    ): PopupWindow {
        val layout = context.inflate(R.layout.color_palette_popup)
        val grid = layout.findViewById<GridView>(R.id.color_palette_grid)
        grid.adapter =
            ColorPaletteAdapter(context.resources.getIntArray(R.array.color_palette), callback)
        return createPopUpWindow(layout, 40 * 5)
    }

    @SuppressLint("InflateParams")
    fun createLineWeightPopup(
        context: Context,
        callback: (Int) -> Unit,
    ): PopupWindow {
        val layout = context.inflate(R.layout.line_weight_popup)
        val list = layout.findViewById<ListView>(R.id.line_weights_list)
        list.adapter =
            LineWeightAdapter(context.resources.getIntArray(R.array.line_weights_dp), callback)
        return createPopUpWindow(layout, 100)
    }

    private fun createPopUpWindow(layout: View, width: Int): PopupWindow {
        return PopupWindow(layout).apply {
            this.width = width.toPx.toInt()
            height = WindowManager.LayoutParams.WRAP_CONTENT
            isOutsideTouchable = true
        }
    }

}