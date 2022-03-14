package com.ick.kalambury.drawing

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ick.kalambury.*
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.R
import com.ick.kalambury.contracts.SpeechRecognitionContract
import com.ick.kalambury.databinding.ActivityGameBinding
import com.ick.kalambury.drawing.GameViewModel.*
import com.ick.kalambury.drawing.GameViewModel.ViewTransitions.*
import com.ick.kalambury.drawing.PopupWindows.createColorPalettePopup
import com.ick.kalambury.drawing.PopupWindows.createLineWeightPopup
import com.ick.kalambury.list.DataAdapter
import com.ick.kalambury.list.ListType
import com.ick.kalambury.list.model.SimpleData
import com.ick.kalambury.service.GameState
import com.ick.kalambury.util.*
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GameActivity : BaseActivity() {

    private val getSpeechRecognitionResults: ActivityResultLauncher<Locale> =
            registerForActivityResult(SpeechRecognitionContract(), ::handleSpeechRecognitionResults)

    private val viewModel: GameViewModel by viewModels()

    private lateinit var binding: ActivityGameBinding
    private lateinit var fullChatAdapter: DataAdapter<SimpleData>
    private lateinit var fullChatLayoutManager: LinearLayoutManager

    @Inject
    lateinit var networkUtils: NetworkUtils

    private var popupMenu: PopupMenu? = null
    private var popupWindow: PopupWindow? = null
    private var dialog: Dialog? = null

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = ActivityGameBinding.inflate(layoutInflater).apply {
            lifecycleOwner = this@GameActivity
            viewModel = this@GameActivity.viewModel
        }

        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        ViewCompat.getWindowInsetsController(binding.root)?.hide(WindowInsetsCompat.Type.statusBars())

        motionLayout.addTransitionListener(object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                if (currentId == R.id.draw_mode_chat_expanded || currentId == R.id.guess_mode_chat_expanded) {
                    binding.fullChat.smoothScrollToPosition(fullChatAdapter.currentList.size - 1)
                }
            }
        })

        DrawableCompat.setTint(DrawableCompat.wrap(binding.buttonColorForeground.drawable),
            ContextCompat.getColor(this,  R.color.cl_black))

        binding.buttonColor.setOnClickListener(::onClick)
        binding.buttonTool.setOnClickListener(::onClick)
        binding.buttonClear.setOnClickListener(::onClick)
        binding.buttonSpeechRecognizer.setOnClickListener(::onClick)
        binding.buttonSend.setOnClickListener(::onClick)
        binding.buttonFullChatSend.setOnClickListener(::onClick)
        binding.buttonMenu.setOnClickListener(::onClick)

        binding.answerField.apply {
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    consume { handleSendText(v, true) }
                }
                false
            }
            filters += arrayOf(InputFilter.LengthFilter(300), CharacterFilter())
        }

        binding.fullChatInputField.apply {
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    consume { handleSendText(v, true) }
                }
                false
            }
            filters += arrayOf(InputFilter.LengthFilter(300), CharacterFilter())
        }

        fullChatAdapter = DataAdapter<SimpleData>(this, lifecycle, ListType.MESSAGE).apply {
            registerAdapterDataObserver(messagesObserver)
        }
        fullChatLayoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        binding.fullChat.apply {
            layoutManager = fullChatLayoutManager
            adapter = fullChatAdapter
        }
        viewModel.fullChatMessages.observe(this) {
            fullChatAdapter.setItems(it)
        }

        viewModel.viewTransitions.observe(this, ::switchViews)
        viewModel.drawEvents.observe(this) { binding.drawingArea.handleDrawEvent(it) }
        viewModel.finishEvent.observe(this, EventObserver {
            when(it) {
                is FinishEvent.Remote -> finishWithDialog(it)
                is FinishEvent.LocalLeave -> handleLeaveGame()
                is FinishEvent.RemoteFinish -> handleFinishGame()
            }
        })

        initInterstitial()
    }

    override fun onStop() {
        super.onStop()

        doLeave(false)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        handleLeaveGame()
    }

    override fun onDestroy() {
        super.onDestroy()

        fullChatAdapter.unregisterAdapterDataObserver(messagesObserver)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.button_send -> handleSendText(binding.answerField, true)
            R.id.button_full_chat_send -> handleSendText(binding.fullChatInputField, false)
            R.id.button_speech_recognizer -> handleSpeechRecognition()
            R.id.button_menu -> handlePopUpMenu()
            R.id.button_color -> handleColorButton()
            R.id.button_tool -> handleLineWeightButton()
            R.id.button_clear -> handleClearButton()
        }
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mo_players -> consume { handlePlayersList() }
            R.id.mo_abandon -> consume { handleAbandonDrawing() }
            R.id.mo_kick -> consume { handleKickPlayersList() }
            R.id.mo_full_chat_open -> consume { viewModel.onFullChatOpened() }
            R.id.mo_full_chat_close -> consume { viewModel.onFullChatClosed() }
            R.id.mo_exit -> consume { handleLeaveGame() }
            else -> false
        }
    }

    private fun handlePlayersList() {
        PlayerDialogFragment().show(supportFragmentManager, null)
    }

    private fun handleKickPlayersList() {
        KickPlayerDialogFragment().show(supportFragmentManager, null)
    }

    private fun handlePopUpMenu() {
        val menu = PopupMenu(this, binding.buttonMenu).apply {
            inflate(R.menu.drawing_menu_options)
            setOnMenuItemClickListener(::onMenuItemClick)
        }

        menu.menu.apply {
            val state = viewModel.gameState?.state
            if (!viewModel.isDrawing || GameState.IN_GAME != state) {
                findItem(R.id.mo_abandon).isVisible = false
            }
            if (GameState.IN_GAME != state || viewModel.gameMode != GameMode.DRAWING_ONLINE || !viewModel.isOperator) {
                findItem(R.id.mo_kick).isVisible = false
            }

            findItem(R.id.mo_full_chat_open).isVisible = !viewModel.isFullChatOpened
            findItem(R.id.mo_full_chat_close).isVisible = viewModel.isFullChatOpened
        }

        menu.show()
        popupMenu = menu
    }

    private fun initInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, BuildConfig.AD_INTERSTITIAL_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(logTag, adError.message)
                mInterstitialAd = null
            }
        })
    }

    private fun handleSpeechRecognitionResults(result: List<String>?) {
        if (result != null) {
            dialog = MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_speech_result_title)
                    .setItems(result.toTypedArray()) { _, which -> handleSendText(result[which], true) }
                    .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
                    .show()
        }
    }

    private fun handleSendText(view: TextView, isAnswer: Boolean) {
        val text = view.text.replace("<.*?>".toRegex(), "")

        hideKeyboard(view)

        view.text = null
        view.clearFocus()

        dialog?.dismiss()

        handleSendText(text, isAnswer)
    }

    private fun handleSendText(text: String, isAnswer: Boolean) {
        viewModel.onLocalChatMessage(text, isAnswer)
    }

    private fun handleColorButton() {
        popupWindow = createColorPalettePopup(this) { color: Int ->
            DrawableCompat.setTint(DrawableCompat.wrap(binding.buttonColorForeground.drawable), color)
            binding.drawingArea.color = color
            popupWindow?.dismiss()
        }
        displayPopUp(popupWindow!!, binding.buttonColor)
    }

    private fun handleLineWeightButton() {
        popupWindow = createLineWeightPopup(this) { value: Int ->
            binding.drawingArea.thickness = value
            popupWindow?.dismiss()
        }
        displayPopUp(popupWindow!!, binding.buttonTool)
    }

    private fun displayPopUp(popup: PopupWindow, anchor: View) {
        popup.showAsDropDown(anchor,
                anchor.width + 5.toPx.toInt(),
                -anchor.height - 5.toPx.toInt())
    }

    private fun handleClearButton() {
        handleConfirmationRequiredAction(R.string.alert_clear_message) { viewModel.onClear() }
    }

    private fun handleAbandonDrawing() {
        handleConfirmationRequiredAction(R.string.alert_abandon_message) { viewModel.onAbandonDrawing() }
    }

    private fun handleFinishGame() {
        PlayerDialogFragment().show(supportFragmentManager, null)
    }

    private fun handleLeaveGame() {
        val msg = if (viewModel.isHost) R.string.alert_close_message_sever else R.string.alert_close_message
        handleConfirmationRequiredAction(msg) { doLeave() }
    }

    private inline fun handleConfirmationRequiredAction(
            @StringRes messageId: Int,
            crossinline onConfirmedAction: () -> Unit
    ) {
        dialog = showMessageDialog(
                messageId = messageId,
                positiveButton = R.string.yes,
                negativeButton = R.string.no
        ) { _, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                onConfirmedAction()
            }
        }
    }

    private fun handleSpeechRecognition() {
        if (!networkUtils.hasNetworkConnection()) {
            showMessageDialog(messageId = R.string.alert_wifi_not_enabled)
            return
        }

        try {
            getSpeechRecognitionResults.launch(viewModel.language.toLocale())
        } catch (ex: ActivityNotFoundException) {
            showMessageDialog(messageId = R.string.alert_speech_not_supported_message)
        }
    }

    private fun switchViews(viewTransitions: ViewTransitions) {
        hidePopups()
        when (viewTransitions) {
            GUESSING_COLLAPSED_TO_GUESSING_EXPANDED -> expandChat(R.id.switch_chat_state_in_guess_mode)
            DRAWING_COLLAPSED_TO_DRAWING_EXPANDED -> expandChat(R.id.switch_chat_state_in_draw_mode)
            GUESSING_EXPANDED_TO_GUESSING_COLLAPSED -> collapseChat(R.id.switch_chat_state_in_guess_mode)
            DRAWING_EXPANDED_TO_DRAWING_COLLAPSED -> collapseChat(R.id.switch_chat_state_in_draw_mode)
            DRAWING_EXPANDED_TO_GUESSING_EXPANDED -> switchMode(R.id.switch_mode_when_chat_expanded, false)
            GUESSING_EXPANDED_TO_DRAWING_EXPANDED -> switchMode(R.id.switch_mode_when_chat_expanded, true)
            DRAWING_COLLAPSED_TO_GUESSING_COLLAPSED -> switchMode(R.id.switch_mode_when_chat_collapsed, false)
            GUESSING_COLLAPSED_TO_DRAWING_COLLAPSED -> switchMode(R.id.switch_mode_when_chat_collapsed, true)
        }
    }

    private fun expandChat(@IdRes transition: Int) {
        val ratio = getDimensionRatio(binding.drawingArea.width, binding.drawingArea.height)

        motionLayout.apply {
            setCanvasViewDimenRatioConstraints(this, ratio)
            setTransition(transition)
            transitionToEnd()
        }
    }

    private fun collapseChat(@IdRes transition: Int) {
        motionLayout.apply {
            clearCanvasViewDimenRatioConstraints(this)
            setTransition(transition)
            transitionToStart()
        }
    }

    private fun switchMode(@IdRes transition: Int, isDrawing: Boolean) {
        motionLayout.apply {
            setTransition(transition)
            if (isDrawing) {
                transitionToStart()
                binding.drawingArea.mode = CanvasView.MODE_DRAW
            } else {
                transitionToEnd()
                binding.drawingArea.mode = CanvasView.MODE_DISPLAY
            }
        }
    }

    private fun setCanvasViewDimenRatioConstraints(layout: MotionLayout, ratio: String) {
        layout.getConstraintSet(R.id.draw_mode_chat_collapsed).setDimensionRatio(R.id.drawing_area, ratio)
        layout.getConstraintSet(R.id.draw_mode_chat_expanded).setDimensionRatio(R.id.drawing_area, ratio)
        layout.getConstraintSet(R.id.guess_mode_chat_collapsed).setDimensionRatio(R.id.drawing_area, ratio)
        layout.getConstraintSet(R.id.guess_mode_chat_expanded).setDimensionRatio(R.id.drawing_area, ratio)
    }

    private fun clearCanvasViewDimenRatioConstraints(layout: MotionLayout) {
        layout.getConstraintSet(R.id.draw_mode_chat_collapsed).setDimensionRatio(R.id.drawing_area, null)
        layout.getConstraintSet(R.id.guess_mode_chat_collapsed).setDimensionRatio(R.id.drawing_area, null)
    }

    private fun finishWithDialog(event: FinishEvent.Remote) {
        showMessageDialog(messageId = event.messageId) { _, _ -> doLeave(event.showAd) }
    }

    private fun doLeave(showAd: Boolean = true) {
        viewModel.onFinish()

        if (showAd) {
            displayClosingAd()
        } else {
            finishActivity()
        }
    }

    private fun displayClosingAd() {
        mInterstitialAd?.apply {
            setImmersiveMode(true)
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(logTag, "Fullscreen ad dismissed.")
                }
                override fun onAdFailedToShowFullScreenContent(e: AdError) {
                    Log.w(logTag, "Failed showing fullscreen ad: $e")
                    finishActivity()
                }
                override fun onAdShowedFullScreenContent() {
                    Log.d(logTag, "Fullscreen ad showed.")
                    finishActivity()
                }
            }
            show(this@GameActivity)
        } ?: finishActivity()
    }

    private fun hidePopups() {
        popupWindow?.dismiss()
        dialog?.dismiss()
        popupMenu?.dismiss()
    }

    private fun hideKeyboard(view: View) {
        ViewCompat.getWindowInsetsController(view)?.hide(WindowInsetsCompat.Type.ime())
    }

    private fun finishActivity() {
        finish()
    }

    private val messagesObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (binding.fullChat.scrollState != RecyclerView.SCROLL_STATE_IDLE) return

            if (fullChatLayoutManager.findLastVisibleItemPosition() == positionStart - 1) {
                binding.fullChat.smoothScrollToPosition(positionStart + itemCount)
            }
        }
    }

    private val motionLayout: MotionLayout
        inline get() = binding.root as MotionLayout

    private fun getDimensionRatio(width: Int, height: Int): String {
        tailrec fun gcd(n1: Int, n2: Int): Int {
            return when (n2) {
                0 -> n1
                else -> gcd(n2, n1 % n2)
            }
        }

        val gcd = gcd(width, height)
        return if (width > height) {
            String.format(Locale.US, "%d:%d", width / gcd, height / gcd)
        } else {
            String.format(Locale.US, "%d:%d", height / gcd, width / gcd)
        }
    }

}