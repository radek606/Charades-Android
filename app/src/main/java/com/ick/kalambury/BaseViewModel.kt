package com.ick.kalambury

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ick.kalambury.logging.Log
import com.ick.kalambury.util.logTag
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class BaseViewModel<T> : ViewModel() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    protected val _swipeRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    val swipeRefreshing: LiveData<Boolean> = _swipeRefreshing

    protected val _snackbarMessage: MutableLiveData<Event<Int>> = MutableLiveData()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarMessage

    protected val _navigationActions: MutableLiveData<Event<T>> =
            MutableLiveData()
    val navigationActions: LiveData<Event<T>> = _navigationActions

    override fun onCleared() {
        Log.d(logTag(), "onCleared()")
        disposables.clear()
    }

}