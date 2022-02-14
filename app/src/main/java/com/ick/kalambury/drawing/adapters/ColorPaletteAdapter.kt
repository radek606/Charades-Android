package com.ick.kalambury.drawing.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ArrayRes
import androidx.core.graphics.drawable.DrawableCompat
import com.ick.kalambury.R
import com.ick.kalambury.util.inflate

class ColorPaletteAdapter(@ArrayRes values: IntArray, callback: (Int) -> Unit) :
    IntegerBasedResourceAdapter(values, callback) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: parent.inflate(R.layout.item_color)
        view.setOnClickListener { callback.invoke(values[position]) }

        val imageView = view.findViewById<ImageView>(R.id.button_color_foreground)

        DrawableCompat.setTint(DrawableCompat.wrap(imageView.drawable), values[position])

        return view
    }
}