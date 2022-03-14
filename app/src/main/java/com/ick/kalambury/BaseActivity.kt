package com.ick.kalambury

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(logTag, "onCreate()")
    }

    override fun onStart() {
        super.onStart()
        Log.d(logTag, "onStart()")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(logTag, "onRestoreInstanceState()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag, "onResume()")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(logTag, "onBackPressed()")
    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag, "onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(logTag, "onStop()")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(logTag, "onRestart()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "onDestroy()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(logTag, "onSaveInstanceState()")

    }

}