package com.ick.kalambury.join

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ick.kalambury.*
import com.ick.kalambury.databinding.FragmentJoinLocalBinding
import com.ick.kalambury.list.DataListFragment
import com.ick.kalambury.list.ListType
import com.ick.kalambury.net.connection.SupportedVersionInfo
import com.ick.kalambury.util.consume
import com.ick.kalambury.util.showMessageDialog
import com.ick.kalambury.util.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinLocalGameFragment : BaseFragment() {

    private val viewModel: JoinLocalGameViewModel by viewModels()

    private lateinit var binding: FragmentJoinLocalBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentJoinLocalBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@JoinLocalGameFragment.viewModel
        }

        val fragment = DataListFragment.newInstance(
            JoinLocalGameViewModel::class.java,
            ListType.DEVICE
        )
        childFragmentManager.beginTransaction()
            .add(R.id.list_fragment_container, fragment)
            .commit()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.mo_refresh -> consume { viewModel.onRefresh() }
                }
                false
            }
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        viewModel.navigationActions.observe(viewLifecycleOwner, EventObserver {
            if (it is JoinGameNavigationActions.NavigateToGameFragment) navigateToGameFragment()
        })

        viewModel.incompatibleVersion.observe(
            viewLifecycleOwner,
            EventObserver(::handleUnsupportedVersion)
        )

        viewModel.snackbarMessage.observe(viewLifecycleOwner, EventObserver {
            binding.root.snackbar(it)
        })
    }

    override fun onStart() {
        super.onStart()

        viewModel.onStart()
    }

    private fun navigateToGameFragment() {
        findNavController().navigate(JoinLocalGameFragmentDirections.actionJoinLocalGameFragmentToGameActivity())
    }

    private fun handleUnsupportedVersion(info: SupportedVersionInfo) {
        val message = if (info.minVersionCode != -1) {
            getString(
                R.string.alert_old_version_message_detailed,
                BuildConfig.VERSION_NAME, info.minVersionName
            )
        } else {
            getString(R.string.alert_old_version_message_plain)
        }

        activity?.showMessageDialog(
            title = R.string.alert_old_version_title,
            messageString = message,
            positiveButton = R.string.alert_old_version_update,
            negativeButton = android.R.string.cancel
        ) { _: DialogInterface, which: Int ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> findNavController().navigate(MainNavDirections.actionGlobalPlayStore())
                DialogInterface.BUTTON_NEGATIVE -> findNavController().popBackStack()
            }
        }
    }

}