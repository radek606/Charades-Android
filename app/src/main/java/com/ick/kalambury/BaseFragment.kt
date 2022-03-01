package com.ick.kalambury

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag

open class BaseFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(logTag(), "onAttach()")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(logTag(), "onCreate()")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(logTag(), "onCreateView()")
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(logTag(), "onViewCreated()")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(logTag(), "onViewStateRestored()")
    }

    override fun onStart() {
        super.onStart()
        Log.d(logTag(), "onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag(), "onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag(), "onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(logTag(), "onStop()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(logTag(), "onSaveInstanceState()")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(logTag(), "onDetach()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(logTag(), "onDestroyView()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag(), "onDestroy()")
    }

}