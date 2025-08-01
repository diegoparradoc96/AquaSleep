package com.diegopcdev.aquasleep.presentation.screens.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegopcdev.aquasleep.service.TimerForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedMinutes = MutableStateFlow(15)
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    private val _timeLeftInSeconds = MutableStateFlow(0)
    val timeLeftInSeconds: StateFlow<Int> = _timeLeftInSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerService: TimerForegroundService? = null
    private var bound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerForegroundService.TimerBinder
            timerService = binder.getService()
            bound = true
            
            // Sincronizar estados con el service
            viewModelScope.launch {
                timerService?.timeLeftInSeconds?.collect { seconds ->
                    _timeLeftInSeconds.value = seconds
                }
            }
            
            viewModelScope.launch {
                timerService?.isTimerRunning?.collect { running ->
                    _isTimerRunning.value = running
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            timerService = null
        }
    }

    init {
        bindToService()
    }

    fun updateSelectedMinutes(minutes: Int) {
        _selectedMinutes.value = minutes
        if (!_isTimerRunning.value) {
            _timeLeftInSeconds.value = minutes * 60
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START_TIMER
            putExtra(TimerForegroundService.EXTRA_MINUTES, _selectedMinutes.value)
        }
        context.startForegroundService(intent)
    }

    fun stopTimer() {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP_TIMER
        }
        context.startService(intent)
    }

    fun extendTimer() {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_EXTEND_TIMER
        }
        context.startService(intent)
    }

    private fun bindToService() {
        val intent = Intent(context, TimerForegroundService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        super.onCleared()
        if (bound) {
            context.unbindService(serviceConnection)
            bound = false
        }
    }
}
