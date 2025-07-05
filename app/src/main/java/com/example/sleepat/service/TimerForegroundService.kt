package com.example.sleepat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.sleepat.MainActivity
import com.example.sleepat.R
import com.example.sleepat.domain.manager.MediaController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class TimerForegroundService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "timer_channel"
        const val ACTION_START_TIMER = "start_timer"
        const val ACTION_STOP_TIMER = "stop_timer"
        const val ACTION_EXTEND_TIMER = "extend_timer"
        const val ACTION_UPDATE_LANGUAGE = "UPDATE_LANGUAGE"
        const val EXTRA_MINUTES = "extra_minutes"
    }

    private val binder = TimerBinder()
    private lateinit var mediaController: MediaController
    private lateinit var notificationManager: NotificationManager
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    
    private val _timeLeftInSeconds = MutableStateFlow(0)
    val timeLeftInSeconds: StateFlow<Int> = _timeLeftInSeconds.asStateFlow()
    
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    private fun getStoredLanguage(): String {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("selected_language", "es") ?: "es"
    }

    private fun createLocalizedContext(): Context {
        val languageCode = getStoredLanguage()
        val locale = Locale(languageCode)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        return createConfigurationContext(config)
    }

    override fun onCreate() {
        super.onCreate()
        mediaController = MediaController(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 15)
                startTimer(minutes)
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
            }
            ACTION_EXTEND_TIMER -> {
                extendTimer()
            }
            ACTION_UPDATE_LANGUAGE -> {
                // Recrear el canal de notificaciones con el nuevo idioma
                createNotificationChannel()
                // Actualizar la notificación si el timer está corriendo
                if (_isTimerRunning.value) {
                    updateNotification()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(minutes: Int) {
        if (_isTimerRunning.value) return
        
        _timeLeftInSeconds.value = minutes * 60
        _isTimerRunning.value = true
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        timerJob = serviceScope.launch {
            while (_timeLeftInSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timeLeftInSeconds.value = _timeLeftInSeconds.value - 1
                updateNotification()
            }
            if (_isTimerRunning.value) {
                _isTimerRunning.value = false
                mediaController.pauseCurrentMedia()
                
                // Detener el servicio foreground y cancelar la notificación
                stopForeground(true) // true para remover la notificación
                
                // Cancelar la notificación explícitamente por seguridad
                notificationManager.cancel(NOTIFICATION_ID)
                
                stopSelf()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _timeLeftInSeconds.value = 0
        
        // Detener el servicio foreground y cancelar la notificación
        stopForeground(true) // true para remover la notificación
        
        // Cancelar la notificación explícitamente por seguridad
        notificationManager.cancel(NOTIFICATION_ID)
        
        stopSelf()
    }

    private fun extendTimer() {
        if (_isTimerRunning.value) {
            // Agregar 10 minutos (600 segundos) al tiempo restante
            _timeLeftInSeconds.value = _timeLeftInSeconds.value + 600
            updateNotification()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localizedContext = createLocalizedContext()
            
            // Eliminar el canal existente si existe para permitir la actualización del idioma
            if (::notificationManager.isInitialized) {
                notificationManager.deleteNotificationChannel(CHANNEL_ID)
            }
            
            val channel = NotificationChannel(
                CHANNEL_ID,
                localizedContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = localizedContext.getString(R.string.notification_channel_description)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val localizedContext = createLocalizedContext()
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, TimerForegroundService::class.java).apply {
                action = ACTION_STOP_TIMER
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val extendIntent = PendingIntent.getService(
            this,
            1, // ID diferente para evitar conflictos
            Intent(this, TimerForegroundService::class.java).apply {
                action = ACTION_EXTEND_TIMER
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(localizedContext.getString(R.string.notification_title))
            .setContentText(formatTime(_timeLeftInSeconds.value))
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_timer_notification, localizedContext.getString(R.string.notification_action_stop), stopIntent)
            .addAction(R.drawable.ic_timer_notification, localizedContext.getString(R.string.notification_action_extend), extendIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        
        // Asegurar que la notificación se cancele cuando el servicio se destruye
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
