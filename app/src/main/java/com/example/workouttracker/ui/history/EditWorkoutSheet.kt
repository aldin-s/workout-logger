package com.example.workouttracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.R
import com.example.workouttracker.ui.theme.RepsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutSheet(
    session: WorkoutSession,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit,
    onSave: (weight: Double, reps: Int, sets: Int) -> Unit
) {
    var weightText by remember { mutableStateOf(session.weight.toString()) }
    var repsText by remember { mutableStateOf(session.reps.toString()) }
    var setsText by remember { mutableStateOf(session.totalSets.toString()) }
    
    var weightError by remember { mutableStateOf<String?>(null) }
    var repsError by remember { mutableStateOf<String?>(null) }
    var setsError by remember { mutableStateOf<String?>(null) }
    
    val originalVolume = session.weight * session.reps * session.totalSets
    
    val currentWeight = weightText.toDoubleOrNull() ?: 0.0
    val currentReps = repsText.toIntOrNull() ?: 0
    val currentSets = setsText.toIntOrNull() ?: 0
    val currentVolume = currentWeight * currentReps * currentSets
    val volumeDifference = currentVolume - originalVolume
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = session.exerciseName.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(
                text = "${session.totalSets} Sätze · ${timeFormat.format(session.startTime)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
            
            // Input fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { 
                        weightText = it
                        weightError = null
                    },
                    label = { Text(stringResource(R.string.weight_label)) },
                    supportingText = { 
                        Text(stringResource(R.string.edit_original_value, "${session.weight} kg"))
                    },
                    isError = weightError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { 
                        repsText = it
                        repsError = null
                    },
                    label = { Text(stringResource(R.string.reps_label)) },
                    supportingText = { 
                        Text(stringResource(R.string.edit_original_value, session.reps.toString()))
                    },
                    isError = repsError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = setsText,
                    onValueChange = { 
                        setsText = it
                        setsError = null
                    },
                    label = { Text(stringResource(R.string.sets_label)) },
                    supportingText = { 
                        Text(stringResource(R.string.edit_original_value, session.totalSets.toString()))
                    },
                    isError = setsError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Volume calculation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.total_volume),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Monochrom: Weiß für positiv, Grau für negativ
                val volumeColor = when {
                    volumeDifference > 0 -> MaterialTheme.colorScheme.onSurface      // Weiß
                    volumeDifference < 0 -> MaterialTheme.colorScheme.onSurfaceVariant // Grau
                    else -> MaterialTheme.colorScheme.onSurface
                }
                
                Text(
                    text = String.format(Locale.getDefault(), "%.0f kg", currentVolume),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = volumeColor
                )
                
                if (volumeDifference != 0.0) {
                    Text(
                        text = if (volumeDifference > 0) {
                            stringResource(R.string.edit_volume_increase, String.format("%.0f", volumeDifference))
                        } else {
                            stringResource(R.string.edit_volume_decrease, String.format("%.0f", abs(volumeDifference)))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = volumeColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
                
                Button(
                    onClick = {
                        val weight = weightText.toDoubleOrNull()
                        val reps = repsText.toIntOrNull()
                        val sets = setsText.toIntOrNull()
                        
                        var hasError = false
                        
                        if (weight == null || weight <= 0) {
                            weightError = "Ungültig"
                            hasError = true
                        }
                        if (reps == null || reps <= 0) {
                            repsError = "Ungültig"
                            hasError = true
                        }
                        if (sets == null || sets <= 0) {
                            setsError = "Ungültig"
                            hasError = true
                        }
                        
                        if (!hasError) {
                            onSave(weight!!, reps!!, sets!!)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun EditWorkoutSheetPreview() {
    val session = WorkoutSession(
        exerciseName = "Kreuzheben",
        weight = 100.0,
        reps = 5,
        totalSets = 3,
        startTime = Date(),
        endTime = Date(),
        sets = emptyList()
    )
    
    RepsTheme(darkTheme = true, dynamicColor = false) {
        EditWorkoutSheet(
            session = session,
            onDismiss = {},
            onSave = { _, _, _ -> }
        )
    }
}
