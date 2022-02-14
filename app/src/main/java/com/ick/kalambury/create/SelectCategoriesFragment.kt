package com.ick.kalambury.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ick.kalambury.BaseFragment
import com.ick.kalambury.EventObserver
import com.ick.kalambury.R
import com.ick.kalambury.databinding.FragmentSelectCategoriesBinding
import com.ick.kalambury.list.DataAdapter
import com.ick.kalambury.list.ListType
import com.ick.kalambury.list.model.WordsSetData
import com.ick.kalambury.util.consume
import com.ick.kalambury.util.snackbar

class SelectCategoriesFragment : BaseFragment() {

    private val viewModel: CreateGameViewModel by hiltNavGraphViewModels(R.id.create_game_nav)

    private lateinit var binding: FragmentSelectCategoriesBinding
    private lateinit var categoriesAdapter: DataAdapter<WordsSetData>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectCategoriesBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@SelectCategoriesFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.apply {
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.mo_select_all -> consume {
                        categoriesAdapter.updateSelection(
                            categoriesAdapter.currentList.map(
                                WordsSetData::id
                            )
                        )
                    }
                    R.id.mo_save -> consume {
                        val canGoBack =
                            viewModel.onCategoriesSelected(categoriesAdapter.selectedItemIds)
                        if (canGoBack) {
                            findNavController().popBackStack()
                        }
                    }
                }
                false
            }
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        categoriesAdapter = DataAdapter(requireContext(), lifecycle, ListType.CATEGORY_SELECTABLE)

        binding.categoriesList.apply {
            adapter = categoriesAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@SelectCategoriesFragment.requireContext(),
                    RecyclerView.VERTICAL
                )
            )
        }

        viewModel.availableSets.observe(viewLifecycleOwner) {
            categoriesAdapter.setItems(it)
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner, EventObserver {
            binding.root.snackbar(it)
        })
    }

}