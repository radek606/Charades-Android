package com.ick.kalambury.join

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ick.kalambury.*
import com.ick.kalambury.databinding.FragmentJoinOnlineBinding
import com.ick.kalambury.join.JoinGameNavigationActions.NavigateToCreateGame
import com.ick.kalambury.join.JoinGameNavigationActions.NavigateToGameFragment
import com.ick.kalambury.list.DataListFragment
import com.ick.kalambury.list.ListType
import com.ick.kalambury.net.connection.SupportedVersionInfo
import com.ick.kalambury.util.consume
import com.ick.kalambury.util.showMessageDialog
import com.ick.kalambury.util.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinOnlineGameFragment : BaseFragment() {

    private val viewModel: JoinOnlineGameViewModel by viewModels()

    private lateinit var binding: FragmentJoinOnlineBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentJoinOnlineBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@JoinOnlineGameFragment.viewModel
        }
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

        binding.viewPager.apply {
            adapter = TableListsPagerAdapter(this@JoinOnlineGameFragment)
            registerOnPageChangeCallback(pageChangeCallback)
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.setText(Tab.values()[position].title)
        }.attach()

        viewModel.navigationActions.observe(viewLifecycleOwner, EventObserver {
            when(it) {
                is NavigateToCreateGame -> navigateToCreateGame(it.gameMode)
                is NavigateToGameFragment -> navigateToGameFragment()
            }
        })

        viewModel.incompatibleVersion.observe(viewLifecycleOwner, EventObserver(::handleUnsupportedVersion))

        viewModel.snackbarMessage.observe(viewLifecycleOwner, EventObserver {
            binding.root.snackbar(it)
        })
    }

    override fun onStart() {
        super.onStart()

        viewModel.onStart()
    }

    private fun navigateToCreateGame(mode: GameMode) {
        findNavController().navigate(JoinOnlineGameFragmentDirections.actionJoinOnlineGameFragmentToCreateGame(mode))
    }

    private fun navigateToGameFragment() {
        findNavController().navigate(JoinOnlineGameFragmentDirections.actionJoinOnlineGameFragmentToGameActivity())
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
            when(which) {
                DialogInterface.BUTTON_POSITIVE -> findNavController().navigate(MainNavDirections.actionGlobalPlayStore())
                DialogInterface.BUTTON_NEGATIVE -> findNavController().popBackStack()
            }
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (Tab.values()[position] == Tab.PUBLIC) {
                binding.createTableFab.show()
            } else {
                binding.createTableFab.hide()
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isEnabled = state == ViewPager.SCROLL_STATE_IDLE
            }
        }
    }

    enum class Tab(val itemView: ListType, @StringRes val title: Int) {
        DEFAULT(ListType.TABLE_DEFAULT, R.string.ja_ltb_tables_default),
        PUBLIC(ListType.TABLE_PUBLIC, R.string.ja_ltb_tables_public);
    }

    class TableListsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            return DataListFragment.newInstance(JoinOnlineGameViewModel::class.java,
                Tab.values()[position].itemView)
        }

        override fun getItemCount(): Int {
            return Tab.values().size
        }
    }
}