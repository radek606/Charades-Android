package com.ick.kalambury.drawing

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ick.kalambury.R
import com.ick.kalambury.databinding.DialogPlayersBinding
import com.ick.kalambury.list.DataAdapter
import com.ick.kalambury.list.ListType

class PlayerDialogFragment : DialogFragment() {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val finishedGameData = viewModel.finishedGameData.value?.getContentIfNotHandled()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(createView(finishedGameData))
            .apply {
                if (finishedGameData != null) {
                    setPositiveButton(R.string.dialog_button_continue) { _, _ -> dismiss() }
                    setNegativeButton(R.string.dialog_button_leave) { _, _ ->
                        dismiss()
                        viewModel.onLeave()
                    }
                } else {
                    setNeutralButton(R.string.dialog_button_close) { _, _ -> dismiss() }
                }
            }.create()
    }

    private fun createView(finishedGameData: GameViewModel.FinishedGameData?): View {
        val binding = DialogPlayersBinding.inflate(LayoutInflater.from(context)).apply {
            lifecycleOwner = requireActivity()
        }
        binding.playersList.adapter = DataAdapter(requireContext(), lifecycle, ListType.PLAYER_DEFAULT,
            finishedGameData?.players?.sorted() ?: viewModel.players.values.sorted()
        )
        finishedGameData?.let { data ->
            binding.winner.apply {
                isGone = false
                text = getString(R.string.dialog_players_winner, data.winnerName)
            }
        }
        return binding.root
    }

}