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
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val timeLeftInSeconds by viewModel.timeLeftInSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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
        }
    }
}
