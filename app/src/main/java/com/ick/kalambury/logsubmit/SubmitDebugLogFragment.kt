package com.ick.kalambury.logsubmit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ick.kalambury.EventObserver
import com.ick.kalambury.R
import com.ick.kalambury.databinding.FragmentSubmitDebugLogBinding
import com.ick.kalambury.util.consume
import com.ick.kalambury.util.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubmitDebugLogFragment : Fragment() {

    private val viewModel: SubmitDebugLogViewModel by viewModels()
    private lateinit var binding: FragmentSubmitDebugLogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSubmitDebugLogBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@SubmitDebugLogFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.mo_submit_logs, -> consume { viewModel.onSendLogs() }
                }
                false
            }
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        viewModel.uploadInProgress.observe(viewLifecycleOwner) {
            binding.toolbar.menu.findItem(R.id.mo_submit_logs).isVisible = !it
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner, EventObserver {
            binding.root.snackbar(it)
        })
    }

}