package com.ick.kalambury.create

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ick.kalambury.*
import com.ick.kalambury.create.CreateGameNavigationActions.*
import com.ick.kalambury.databinding.FragmentCreateBinding
import com.ick.kalambury.list.DataAdapter
import com.ick.kalambury.list.ListType
import com.ick.kalambury.list.model.WordsSetData
import com.ick.kalambury.net.connection.SupportedVersionInfo
import com.ick.kalambury.util.consume
import com.ick.kalambury.util.showMessageDialog
import com.ick.kalambury.util.snackbar

class CreateGameFragment : BaseFragment() {

    private val viewModel: CreateGameViewModel by hiltNavGraphViewModels(R.id.create_game_nav)

    private lateinit var binding: FragmentCreateBinding
    private lateinit var categoriesAdapter: DataAdapter<WordsSetData>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentCreateBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@CreateGameFragment.viewModel
            gameMode = this@CreateGameFragment.viewModel.gameMode
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            if (viewModel.gameMode == GameMode.DRAWING_ONLINE) {
                title = getString(R.string.ca_ltb_table_title)
                menu.findItem(R.id.mo_create).title = getString(R.string.ca_lcvtf_create)
            } else {
                title = getString(R.string.ca_ltb_game_title)
                menu.findItem(R.id.mo_create).title = getString(R.string.ca_lcvtf_start)
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.mo_create, -> consume { viewModel.onCompleted() }
                }
                false
            }
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        if (viewModel.gameMode == GameMode.SHOWING) {
            binding.content.timePickerCardLabel.setText(R.string.ca_lcvtf_time_showing)
        } else {
            binding.content.timePickerCardLabel.setText(R.string.ca_lcvtf_time_drawing)
        }

        categoriesAdapter = DataAdapter(requireContext(), lifecycle, ListType.CATEGORY_DEFAULT)

        binding.content.catCardList.apply {
            adapter = categoriesAdapter
            addItemDecoration(DividerItemDecoration(this@CreateGameFragment.requireContext(),
                RecyclerView.VERTICAL))
        }

        viewModel.navigationActions.observe(viewLifecycleOwner, EventObserver {
            when(it) {
                is NavigateToShowingFragment -> navigateToShowingFragment(it.gameConfig)
                is NavigateToGameFragment -> navigateToGameFragment()
                is NavigateToSelectCategories -> navigateToSelectCategories()
            }
        })

        viewModel.selectedSets.observe(viewLifecycleOwner) { categoriesAdapter.setItems(it) }

        viewModel.createInProgress.observe(viewLifecycleOwner) {
            binding.toolbar.menu.findItem(R.id.mo_create).isVisible = !it
        }

        viewModel.incompatibleVersion.observe(viewLifecycleOwner, EventObserver(::handleUnsupportedVersion))

        viewModel.snackbarMessage.observe(viewLifecycleOwner, EventObserver {
            binding.root.snackbar(it)
        })
    }

    private fun navigateToSelectCategories() {
        findNavController().navigate(CreateGameFragmentDirections.actionCreateFragmentToSelectCategoriesFragment())
    }

    private fun navigateToGameFragment() {
        findNavController().navigate(CreateGameFragmentDirections.actionCreateFragmentToGameActivity())
    }

    private fun navigateToShowingFragment(gameConfig: GameConfig) {
        findNavController().navigate(CreateGameFragmentDirections.actionCreateFragmentToShowingFragment(gameConfig))
    }

    private fun handleUnsupportedVersion(info: SupportedVersionInfo) {
        activity?.showMessageDialog(
                title = R.string.alert_old_version_title,
                messageString = getString(R.string.alert_old_version_message_detailed,
                        BuildConfig.VERSION_NAME, info.minVersionName),
                positiveButton = R.string.alert_old_version_update,
                negativeButton = android.R.string.cancel
        ) { _: DialogInterface, which: Int ->
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> findNavController().navigate(MainNavDirections.actionGlobalPlayStore())
                DialogInterface.BUTTON_NEGATIVE -> findNavController().navigate(CreateGameFragmentDirections.actionCreateFragmentToMainMenuFragment())
            }
        }
    }

}