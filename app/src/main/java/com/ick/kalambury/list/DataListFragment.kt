package com.ick.kalambury.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ick.kalambury.BaseFragment
import com.ick.kalambury.databinding.FragmentDataListBinding
import com.ick.kalambury.join.JoinGameViewModel
import com.ick.kalambury.list.model.ListableData

@Suppress("UNCHECKED_CAST")
class DataListFragment<T : ListableData> : BaseFragment() {

    private lateinit var type: ListType
    private lateinit var viewModelClass: Class<out JoinGameViewModel<T>>

    private val viewModel by lazy {
        ViewModelProvider(requireParentFragment())[viewModelClass]
    }

    private lateinit var binding: FragmentDataListBinding
    private lateinit var listAdapter: DataAdapter<T>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().let {
            type = ListType.valueOf(requireNotNull(it.getString(EXTRA_VIEW_PROVIDER)))
            viewModelClass = it.getSerializable(EXTRA_VIEW_MODEL_CLASS) as Class<out JoinGameViewModel<T>>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentDataListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = DataAdapter(requireContext(), lifecycle, type) {
            viewModel.onItemClicked(it.data as T)
        }

        binding.list.apply {
            adapter = listAdapter
            addItemDecoration(DividerItemDecoration(this@DataListFragment.requireContext(),
                RecyclerView.VERTICAL))
        }

        viewModel.getDataList(type.key)
            .observe(viewLifecycleOwner) { items: List<T>? ->
                listAdapter.setItems(items) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
    }

    companion object {

        private const val EXTRA_VIEW_MODEL_CLASS = "view_model_class"
        private const val EXTRA_VIEW_PROVIDER = "view_provider"

        fun <T : ListableData> newInstance(
            viewModelClass: Class<out JoinGameViewModel<T>>,
            itemView: ListType
        ): DataListFragment<T> {
            return DataListFragment<T>().apply {
                arguments = bundleOf(
                    EXTRA_VIEW_MODEL_CLASS to viewModelClass,
                    EXTRA_VIEW_PROVIDER to itemView.name
                )
            }
        }

    }
}