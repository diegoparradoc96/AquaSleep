package com.example.sleepat.presentation.screens.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.sleepat.domain.manager.DeviceAdminManager
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val timeLeftInSeconds by viewModel.timeLeftInSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val isAdminPermissionActive by viewModel.isAdminPermissionActive.collectAsState()
    val context = LocalContext.current

    // Re-check permission status when the user returns to the app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAdminPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Collect events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TimerEvent.RequestAdminPermission -> {
                    val deviceAdminManager = DeviceAdminManager(context)
                    val intent = deviceAdminManager.createAdminPermissionIntent()
                    context.startActivity(intent)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTimerRunning) {
                val hours = timeLeftInSeconds / 3600
                val minutes = (timeLeftInSeconds % 3600) / 60
                val seconds = timeLeftInSeconds % 60
                Text(
                    text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                    style = MaterialTheme.typography.headlineLarge
                )
            } else {
                CircularTimeSelector(
                    modifier = Modifier.fillMaxSize(),
                    initialMinutes = selectedMinutes,
                    onTimeChange = { viewModel.updateSelectedMinutes(it) }
                ) {
                    Text(
                        text = "${selectedMinutes}m",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!isAdminPermissionActive) {
            Card(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "Para suspender el dispositivo, la aplicación necesita permisos de administrador. Por favor, actívalos para continuar.",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (isAdminPermissionActive) {
                Button(
                    onClick = { viewModel.startTimer() },
                    enabled = !isTimerRunning
                ) {
                    Text("Start")
                }
                Button(
                    onClick = { viewModel.stopTimer() },
                    enabled = isTimerRunning
                ) {
                    Text("Stop")
                }
            } else {
                Button(onClick = { viewModel.requestAdminPermission() }) {
                    Text("Conceder Permiso")
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
