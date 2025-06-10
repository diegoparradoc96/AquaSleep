package com.example.sleepat.presentation.screens.timer

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft: StateFlow<Long> = _timeLeft

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private var countDownTimer: CountDownTimer? = null

    fun startTimer(durationMillis: Long) {
        if (_isTimerRunning.value) return

        _timeLeft.value = durationMillis
        _isTimerRunning.value = true

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
            }

            override fun onFinish() {
                _timeLeft.value = 0
                _isTimerRunning.value = false
                // TODO: Implement device sleep functionality here
            }
        }.start()
    }

    fun stopTimer() {
        countDownTimer?.cancel()
        _isTimerRunning.value = false
        // Reset time or keep current timeLeft based on desired behavior
        // _timeLeft.value = 0L 
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
