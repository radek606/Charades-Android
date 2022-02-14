package com.ick.kalambury.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContract
import java.util.*

class SpeechRecognitionContract : ActivityResultContract<Locale, List<String>?>() {

    override fun createIntent(context: Context, locale: Locale): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<String>? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null
        else intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
    }

}