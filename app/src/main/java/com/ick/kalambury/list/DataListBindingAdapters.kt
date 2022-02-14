package com.ick.kalambury.list

import android.graphics.Color
import android.graphics.Typeface
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ick.kalambury.R

@BindingAdapter(value = ["isNew", "isUpdated", "itemMode"], requireAll = true)
fun setWordsSetLabel(view: TextView, isNew: Boolean, isUpdated: Boolean, itemMode: ListType.ItemMode) {
    if (itemMode == ListType.ItemMode.SELECTABLE) {
        when {
            isNew -> {
                view.setText(R.string.ca_category_label_new)
                view.visibility = RelativeLayout.VISIBLE
            }
            isUpdated -> {
                view.setText(R.string.ca_category_label_updated)
                view.visibility = RelativeLayout.VISIBLE
            }
            else -> {
                view.visibility = RelativeLayout.GONE
            }
        }
    } else {
        view.visibility = RelativeLayout.GONE
    }
}

@BindingAdapter(value = ["players", "playersLimit"], requireAll = true)
fun setPlayersLabel(view: TextView, players: Int, playersLimit: Int) {
    view.text = view.context.getString(R.string.jlr_ltf_players, players, playersLimit)
}

@BindingAdapter(value = ["name", "active", "operator", "winner"], requireAll = false)
fun markPlayerAsOperator(view: TextView, name: String?, active: Boolean, operator: Boolean, winner: Boolean) {
    view.setTextColor(if (active) Color.BLACK else Color.GRAY)
    view.setTypeface(view.typeface, if (winner) Typeface.BOLD else Typeface.NORMAL)
    if (!name.isNullOrEmpty()) {
        if (active && operator) {
            view.text = view.context.getString(R.string.player_operator_label, name)
        } else {
            view.text = name
        }
    }
}