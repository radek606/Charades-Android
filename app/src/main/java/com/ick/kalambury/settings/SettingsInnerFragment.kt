package com.ick.kalambury.settings

import android.os.Bundle
import android.os.Vibrator
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.MainNavDirections
import com.ick.kalambury.R
import com.ick.kalambury.util.CharacterFilter
import com.ick.kalambury.util.consume
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsInnerFragment : PreferenceFragmentCompat() {

    @set:Inject
    var vibrator: Vibrator? = null

    @Inject
    lateinit var keys: PreferenceKeys

    private val viewModel: SettingsViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<EditTextPreference>(keys.nickname.name)?.apply {
            setOnBindEditTextListener {
                it.filters = arrayOf(LengthFilter(30), CharacterFilter())
            }
            setOnPreferenceChangeListener { _, newValue ->
                consume { viewModel.onNickname(newValue as? String) }
            }
        }
        viewModel.nickname.observe(viewLifecycleOwner) {
            findPreference<EditTextPreference>(keys.nickname.name)?.apply {
                text = it
            }
        }

        findPreference<ListPreference>(keys.wordsLanguage.name)?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                consume { viewModel.onLanguage(newValue as String) }
            }
        }
        viewModel.language.observe(viewLifecycleOwner) {
            findPreference<ListPreference>(keys.wordsLanguage.name)?.apply {
                value = it
            }
        }

        findPreference<SeekBarPreference>(keys.chatSize.name)?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                consume { viewModel.onChatSize(newValue as Int) }
            }
        }
        viewModel.chatSize.observe(viewLifecycleOwner) {
            findPreference<SeekBarPreference>(keys.chatSize.name)?.apply {
                value = it
            }
        }

        findPreference<SwitchPreferenceCompat>(keys.vibrationNotificationEnabled.name)?.apply {
            vibrator?.let {
                if (it.hasVibrator()) {
                    setOnPreferenceChangeListener { _, newValue ->
                        consume { viewModel.onVibrationNotification(newValue as Boolean) }
                    }
                } else {
                    setSummary(R.string.oa_o_vibrate_denied)
                    isEnabled = false
                }
            } ?: run {
                setSummary(R.string.oa_o_vibrate_denied)
                isEnabled = false
            }
        }
        viewModel.notifications.observe(viewLifecycleOwner) {
            findPreference<SwitchPreferenceCompat>(keys.vibrationNotificationEnabled.name)?.apply {
                isChecked = it
            }
        }

        findPreference<SwitchPreferenceCompat>(keys.sendUsageStatistics.name)?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                consume { viewModel.onSendUsageStatistics(newValue as Boolean) }
            }
        }
        viewModel.statistics.observe(viewLifecycleOwner) {
            findPreference<SwitchPreferenceCompat>(keys.sendUsageStatistics.name)?.apply {
                isChecked = it
            }
        }

        findPreference<Preference>("pref_submit_logs")?.apply {
            setOnPreferenceClickListener {
                consume {
                    findNavController().navigate(MainNavDirections.actionGlobalSubmitDebugLogFragment())
                }
            }
        }

        findPreference<Preference>("pref_privacy")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                consume {
                    val webView = WebView(requireContext()).apply {
                        loadUrl("file:///android_asset/policies/policy.txt")
                    }

                    MaterialAlertDialogBuilder(requireContext())
                        .setView(webView)
                        .create()
                        .show()
                }
            }

        findPreference<Preference>("pref_version")?.apply {
            title = getString(R.string.aa_ltf_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE)
        }
    }

}