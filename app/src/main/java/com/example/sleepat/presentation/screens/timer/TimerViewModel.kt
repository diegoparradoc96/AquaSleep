package com.example.sleepat.presentation.screens.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleepat.domain.manager.DeviceAdminManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TimerEvent {
    object RequestAdminPermission : TimerEvent()
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val deviceAdminManager: DeviceAdminManager
) : ViewModel() {

    private val _selectedMinutes = MutableStateFlow(15)
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    private val _timeLeftInSeconds = MutableStateFlow(_selectedMinutes.value * 60)
    val timeLeftInSeconds: StateFlow<Int> = _timeLeftInSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isAdminPermissionActive = MutableStateFlow(deviceAdminManager.isAdminActive())
    val isAdminPermissionActive: StateFlow<Boolean> = _isAdminPermissionActive.asStateFlow()

    private val _events = Channel<TimerEvent>()
    val events = _events.receiveAsFlow()

    private var timerJob: Job? = null

    fun updateSelectedMinutes(minutes: Int) {
        _selectedMinutes.value = minutes
        if (!_isTimerRunning.value) {
            _timeLeftInSeconds.value = minutes * 60
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value || !isAdminPermissionActive.value) return

        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            var seconds = _timeLeftInSeconds.value
            while (seconds > 0) {
                delay(1000)
                seconds--
                _timeLeftInSeconds.value = seconds
            }
            _isTimerRunning.value = false
            deviceAdminManager.lockScreen()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _timeLeftInSeconds.value = _selectedMinutes.value * 60
    }

    fun checkAdminPermission() {
        _isAdminPermissionActive.value = deviceAdminManager.isAdminActive()
    }

    fun requestAdminPermission() {
        viewModelScope.launch {
            _events.send(TimerEvent.RequestAdminPermission)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
