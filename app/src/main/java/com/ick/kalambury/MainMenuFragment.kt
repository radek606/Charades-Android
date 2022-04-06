package com.ick.kalambury

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ick.kalambury.MainMenuNavigationActions.*
import com.ick.kalambury.databinding.FragmentMainMenuBinding
import com.ick.kalambury.util.showMessageDialog
import com.ick.kalambury.util.snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainMenuFragment : BaseFragment() {

    @set:Inject
    var locationManager: LocationManager? = null

    private val requestPermissions =
        registerForActivityResult(RequestMultiplePermissions(), ::handlePermissionRequestResult)

    private val viewModel: MainMenuViewModel by viewModels()
    private lateinit var binding: FragmentMainMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentMainMenuBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@MainMenuFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigationActions.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is NavigateToNicknameDialogAction -> showNicknameDialog()
                is NavigateToCreateGame -> navigateToCreateGame(it.gameMode)
                is NavigateToJoinLocalGame -> navigateToJoinLocalGame()
                is NavigateToJoinOnlineGame -> navigateToJoinOnlineGame()
                is NavigateToSettings -> navigateToSettings()
                is NavigateToHelp -> { }
            }
        })

        viewModel.snackbarMessage.observe(viewLifecycleOwner, EventObserver {
            binding.root.snackbar(it)
        })

        val savedStateHandle =
            findNavController().currentBackStackEntry?.savedStateHandle

        savedStateHandle?.getLiveData<String>(TextInputDialogFragment.TEXT_INPUT_DIALOG_RESULT)
            ?.observe(viewLifecycleOwner) {
                val nickname = it
                savedStateHandle.remove<String>(TextInputDialogFragment.TEXT_INPUT_DIALOG_RESULT)
                viewModel.onNickname(nickname)
            }
    }

    override fun onStart() {
        super.onStart()

        viewModel.prompt.observe(this, EventObserver {
            it.launchPrompt(requireActivity(), findNavController())
        })
    }

    private fun showNicknameDialog() {
        findNavController().navigate(
            MainNavDirections.actionGlobalTextInputDialog(
                title = R.string.dialog_nickname_title,
                buttonText = R.string.dialog_button_continue,
                inputLengthLimit = 30
            )
        )
    }

    private fun navigateToCreateGame(mode: GameMode) {
        if (mode == GameMode.DRAWING_LOCAL) {
            checkPrerequisites {
                findNavController().navigate(MainMenuFragmentDirections.mainMenuFragmentToCreateGame(mode))
            }
        } else {
            findNavController().navigate(MainMenuFragmentDirections.mainMenuFragmentToCreateGame(mode))
        }
    }

    private fun navigateToJoinLocalGame() {
        checkPrerequisites {
            findNavController().navigate(MainMenuFragmentDirections.actionMainMenuFragmentToJoinLocalGameFragment())
        }
    }

    private fun navigateToJoinOnlineGame() {
        findNavController().navigate(MainMenuFragmentDirections.actionMainMenuFragmentToJoinOnlineGameFragment())
    }

    private fun navigateToSettings() {
        findNavController().navigate(MainNavDirections.actionGlobalSettingsFragment())
    }

    private inline fun checkPrerequisites(grantedAction: () -> Unit) {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            else ->
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        when {
            hasPermissions(permissions) -> {
                if (isLocationEnabled()) {
                    grantedAction.invoke()
                } else {
                    showLocationDisabledDialog()
                }
            }
            shouldShowRationale(permissions) -> {
                showRequestPermissionRationale(permissions)
            }
            else -> {
                requestPermissions.launch(permissions)
            }
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowRationale(permissions: Array<String>): Boolean {
        return permissions.any { shouldShowRequestPermissionRationale(it) }
    }

    private fun showRequestPermissionRationale(permissions: Array<String>) {
        requireContext().showMessageDialog(
            messageId = R.string.alert_permission_localization_request_message,
            cancelable = false
        ) { _, _ -> requestPermissions.launch(permissions) }
    }

    private fun showPermissionDeniedDialog() {
        requireContext().showMessageDialog(
            messageId = R.string.alert_permission_localization_denied_message,
            cancelable = false
        )
    }

    private fun showLocationDisabledDialog() {
        requireContext().showMessageDialog(
            title = R.string.alert_location_not_enabled_title,
            messageId = R.string.alert_location_not_enabled_message,
            cancelable = true
        )
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager?.let { LocationManagerCompat.isLocationEnabled(it) } ?: false
    }

    private fun handlePermissionRequestResult(permissions: Map<String, Boolean>) {
        if (permissions.values.all { it }) {
            viewModel.onPermissionRequestResult()
        } else {
            showPermissionDeniedDialog()
        }
    }

}