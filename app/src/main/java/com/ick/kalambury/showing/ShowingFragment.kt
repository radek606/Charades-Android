package com.ick.kalambury.showing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.ick.kalambury.BaseFragment
import com.ick.kalambury.R
import com.ick.kalambury.databinding.FragmentShowingBinding
import com.ick.kalambury.util.consume
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowingFragment : BaseFragment() {

    private val viewModel: ShowingViewModel by viewModels()

    private lateinit var binding: FragmentShowingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentShowingBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ShowingFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.mo_next_word, -> consume { viewModel.onNextWord() }
                }
                false
            }
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        binding.saAdView.loadAd(AdRequest.Builder().build())
    }

    override fun onDestroy() {
        super.onDestroy()

        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

}