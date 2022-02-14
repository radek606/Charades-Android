package com.ick.kalambury.drawing

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ick.kalambury.R
import com.ick.kalambury.list.DataAdapter
import com.ick.kalambury.list.ListType
import com.ick.kalambury.list.model.Player
import com.ick.kalambury.util.inflate

class KickPlayerDialogFragment : DialogFragment() {

    private val viewModel: GameViewModel by activityViewModels()

    private lateinit var adapter: DataAdapter<Player>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_kick_players_title)
            .setView(createView())
            .setPositiveButton(R.string.dialog_button_kick) { _, _ -> onKick() }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun createView(): View {
        val view = requireContext().inflate(R.layout.recycler_view_list)
        val playersList: RecyclerView = view.findViewById(R.id.recycler_list)

        adapter = DataAdapter(requireContext(), lifecycle, ListType.PLAYER_SELECTABLE,
            viewModel.players.values.filter { !it.operator && it.active })

        playersList.adapter = adapter

        return view
    }

    private fun onKick() {
        dismiss()
        viewModel.onKickPlayers(adapter.selectedItemIds)
    }

}