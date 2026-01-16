package com.example.workouttracker.ui.timer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.R
import com.example.workouttracker.ui.theme.RepsTheme

@Composable
fun TimerScreen(
    state: TimerState,
    onSetCompleted: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Exercise Name Header
            Text(
                text = state.exerciseName.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Divider line
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 140.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Weight Display
            Text(
                text = stringResource(R.string.weight_format, state.weight),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Timer Display
            Text(
                text = formatTime(state.timeLeftInMillis),
                fontSize = 72.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sets Display
            Text(
                text = stringResource(R.string.set_format, state.currentSet, state.totalSets),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Done Button
            OutlinedButton(
                onClick = onSetCompleted,
                enabled = !state.isTimerRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (!state.isTimerRunning) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                )
            ) {
                Text(
                    text = if (state.isTimerRunning) {
                        stringResource(R.string.pause_running)
                    } else {
                        stringResource(R.string.set_done)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    return String.format("%02d:%02d", seconds / 60, seconds % 60)
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun TimerScreenPreview() {
    RepsTheme(darkTheme = true, dynamicColor = false) {
        TimerScreen(
            state = TimerState(
                exerciseName = "Bankdrücken",
                weight = 80.0,
                currentSet = 1,
                totalSets = 3,
                timeLeftInMillis = 60000,
                isTimerRunning = true
            ),
            onSetCompleted = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun TimerScreenReadyPreview() {
    RepsTheme(darkTheme = true, dynamicColor = false) {
        TimerScreen(
            state = TimerState(
                exerciseName = "Kreuzheben",
                weight = 120.0,
                currentSet = 2,
                totalSets = 5,
                timeLeftInMillis = 0,
                isTimerRunning = false
            ),
            onSetCompleted = {}
        )
    }
}
