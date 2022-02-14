package com.ick.kalambury

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ick.kalambury.util.CharacterFilter
import com.ick.kalambury.util.inflate

class TextInputDialogFragment : AppCompatDialogFragment() {

    private lateinit var inputField: EditText

    private val args: TextInputDialogFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(args.title)
            .setView(createView(requireContext()))
            .setPositiveButton(args.buttonText, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun createView(context: Context): View {
        return context.inflate(R.layout.dialog_text_input).apply {
            inputField = findViewById<EditText>(R.id.input_field).apply {
                setText(args.defaultText)
                filters += CharacterFilter()
                if (args.inputLengthLimit != 0) {
                    filters += LengthFilter(args.inputLengthLimit)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val dialog = requireDialog() as AlertDialog
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            if (isInputValid) {
                findNavController().apply {
                    popBackStack()
                    currentBackStackEntry?.savedStateHandle?.set(
                        TEXT_INPUT_DIALOG_RESULT, inputField.text.toString())
                }
            } else {
                inputField.error = getString(R.string.alert_empty_input)
            }
        }
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private val isInputValid: Boolean
        get() = !TextUtils.isEmpty(inputField.text)

    companion object {
        const val TEXT_INPUT_DIALOG_RESULT = "extra_text"
    }
}