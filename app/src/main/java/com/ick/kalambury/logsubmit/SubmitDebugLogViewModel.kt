package com.ick.kalambury.logsubmit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ick.kalambury.BaseViewModel
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.Event
import com.ick.kalambury.R
import com.ick.kalambury.net.api.RestApiManager
import com.ick.kalambury.net.api.exceptions.TooManyRequestsException
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.util.log.Log
import com.ick.kalambury.util.log.logTag
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class SubmitDebugLogViewModel @Inject constructor(
    repository: SubmitDebugLogRepository,
    private val apiManager: RestApiManager,
    private val schedulerProvider: SchedulerProvider,
) : BaseViewModel<Unit>() {

    private val _logContent: MutableLiveData<String> = MutableLiveData()
    val logContent: LiveData<String> = _logContent

    private val _uploadInProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val uploadInProgress: LiveData<Boolean> =
        Transformations.distinctUntilChanged(_uploadInProgress)

    private var uploaded: Boolean = false

    init {
        disposables += repository.getComposedLog()
            .subscribeOn(schedulerProvider.io())
            .subscribe(_logContent::postValue) {
                Log.w(logTag, "Failed getting composed log.", it)
            }
    }

    fun onSendLogs() {
        if (uploaded) {
            _snackbarMessage.value = Event(R.string.log_submit_already_sent_toast)
            return
        }

        _logContent.value?.let {
            _uploadInProgress.value = true
            uploadContent("text/plain", it)
        }
    }

    private fun uploadContent(contentType: String, content: String) {
        val name = "${BuildConfig.VERSION_NAME}_${System.currentTimeMillis()}.txt"
        val body = content.toRequestBody(contentType.toMediaType())
        val file = MultipartBody.Part.createFormData("file", name, body)

        disposables += apiManager.logSubmit(file, body)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribeBy(
                onComplete = {
                    uploaded = true
                    _uploadInProgress.value = false
                    _snackbarMessage.value = Event(R.string.log_submit_success_toast)
                },
                onError = {
                    Log.w(logTag, "Failed uploading logs.", it)
                    _uploadInProgress.value = false
                    _snackbarMessage.value = when(it) {
                        is TooManyRequestsException -> Event(R.string.log_submit_to_many_requests_toast)
                        else -> Event(R.string.log_submit_failed_toast)
                    }
                }
            )
    }

}