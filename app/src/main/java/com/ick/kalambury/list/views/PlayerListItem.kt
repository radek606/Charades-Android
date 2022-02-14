package com.ick.kalambury.list.views

import android.content.Context
import android.util.AttributeSet
import com.ick.kalambury.databinding.ListItemPlayerScoreBinding
import com.ick.kalambury.list.model.Player

class PlayerListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ListItem<Player, ListItemPlayerScoreBinding>(context, attrs, defStyleAttr, defStyleRes) {

    override val data: Player
        get() = binding.data as Player

}