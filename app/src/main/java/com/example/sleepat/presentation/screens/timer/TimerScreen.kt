package com.example.sleepat.presentation.screens.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel
) {
    val timeLeftInSeconds by viewModel.timeLeftInSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()

    // displayTime is now in milliseconds, calculated directly from timeLeftInSeconds
    val displayTime = timeLeftInSeconds * 1000L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularTimeSelector(
            initialMinutes = selectedMinutes,
            onTimeChange = { newMinutes ->
                // The time can only be changed when the timer is not running
                if (!isTimerRunning) {
                    viewModel.updateSelectedMinutes(newMinutes)
                }
            }
        ) {
            Text(
                text = formatTime(displayTime),
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.startTimer() },
                enabled = !isTimerRunning
            ) {
                Text(text = "Start")
            }
            Button(
                onClick = { viewModel.stopTimer() },
                enabled = isTimerRunning
            ) {
                Text(text = "Stop")
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
