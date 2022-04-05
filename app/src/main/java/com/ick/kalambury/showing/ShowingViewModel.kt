package com.ick.kalambury.showing

import android.os.Vibrator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.ick.kalambury.BaseViewModel
import com.ick.kalambury.GameConfig
import com.ick.kalambury.R
import com.ick.kalambury.service.RxCountDownTimer
import com.ick.kalambury.settings.MainPreferenceStorage
import com.ick.kalambury.util.Label
import com.ick.kalambury.util.SchedulerProvider
import com.ick.kalambury.util.TimerMode
import com.ick.kalambury.words.InstanceId
import com.ick.kalambury.wordsrepository.WordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ShowingViewModel @Inject constructor(
    preferenceStorage: MainPreferenceStorage,
    private val wordsRepository: WordsRepository,
    private val vibrator: Vibrator?,
    private val schedulerProvider: SchedulerProvider,
    stateHandle: SavedStateHandle,
) : BaseViewModel<Unit>() {

    private val gameConfig: GameConfig = stateHandle["gameConfig"] ?: throw IllegalStateException("No game config")

    private var vibrationEnabled: Boolean = false

    private val _categoryLabel: MutableLiveData<Label> = MutableLiveData()
    val categoryLabel: LiveData<Label> = _categoryLabel

    private val _wordLabel: MutableLiveData<Label> = MutableLiveData(Label.res(R.string.sa_ltf_password_init_val))
    val wordLabel: LiveData<Label> = _wordLabel

    private val _timeUpLabel: MutableLiveData<Label?> = MutableLiveData()
    val timeUpLabel: LiveData<Label?> = _timeUpLabel

    private val _timer: MutableLiveData<Int> = MutableLiveData(0)
    val timer: LiveData<Int> = _timer

    private val _timerMode: MutableLiveData<TimerMode> = MutableLiveData(TimerMode.NORMAL)
    val timerMode: LiveData<TimerMode> = _timerMode

    private var gameTimer: RxCountDownTimer? = null

    private val vibrate
        get() = vibrator != null && vibrator.hasVibrator() && vibrationEnabled

    init {
        disposables += preferenceStorage.vibrationEnabled
            .firstOrError()
            .observeOn(schedulerProvider.main())
            .subscribe { state -> vibrationEnabled = state }

        disposables += wordsRepository.getWordsObservable()
            .observeOn(schedulerProvider.main())
            .subscribe { word ->
                _categoryLabel.value = Label.res(R.string.sa_ltf_category, false, word.setName!!)
                _wordLabel.value = Label.text(word.wordString)
            }
    }

    fun onNextWord() {
        wordsRepository.requestWord(InstanceId(gameConfig.gameMode, gameConfig.language))
        setGameTimer(gameConfig.roundTime)
    }

    private fun setGameTimer(seconds: Int) {
        gameTimer?.cancel()

        if (seconds <= 0) {
            return
        }

        _timerMode.value = TimerMode.NORMAL
        _timeUpLabel.value = null

        gameTimer = object : RxCountDownTimer(seconds.toLong(), 1, TimeUnit.SECONDS, schedulerProvider.main()) {
            override fun onTick(tick: Long) {
                _timer.value = tick.toInt()
                if (tick == 15L && vibrate) {
                    vibrator!!.vibrate(VIBRATE_SHORT, -1)
                }
                if (tick <= 15L) {
                    _timerMode.value = TimerMode.WARN
                }
            }

            override fun onFinish() {
                _timerMode.value = TimerMode.GONE
                _timeUpLabel.value = Label.res(R.string.sa_ltf_timer_end)
                if (vibrate) {
                    vibrator!!.vibrate(VIBRATE_LONG, -1)
                }
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()

        gameTimer?.cancel()
        wordsRepository.saveWordsInstance(InstanceId(gameConfig.gameMode, gameConfig.language))
            .subscribe()
    }

    companion object {
        private val VIBRATE_SHORT = longArrayOf(0, 500)
        private val VIBRATE_LONG = longArrayOf(0, 500, 100, 500, 100, 500)
    }

}