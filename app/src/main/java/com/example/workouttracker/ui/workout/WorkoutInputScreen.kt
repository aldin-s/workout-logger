package com.example.workouttracker.ui.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workouttracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInputScreen(
    state: WorkoutInputState,
    onExerciseSelected: (Exercise) -> Unit,
    onCustomExerciseNameChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onPauseTimeChanged: (String) -> Unit,
    onSetsChanged: (String) -> Unit,
    onStartWorkout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.workout_input_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Selection Section
            Text(
                text = stringResource(R.string.select_exercise),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Exercise Cards Grid
            ExerciseCardsGrid(
                selectedExercise = state.selectedExercise,
                onExerciseSelected = onExerciseSelected
            )
            
            // Custom Exercise Input (only visible when Custom is selected)
            if (state.selectedExercise is Exercise.Custom) {
                OutlinedTextField(
                    value = state.customExerciseName,
                    onValueChange = onCustomExerciseNameChanged,
                    label = { Text(stringResource(R.string.custom_exercise_hint)) },
                    isError = state.exerciseError != null,
                    supportingText = state.exerciseError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Exercise error (for selection error)
            if (state.exerciseError != null && state.selectedExercise !is Exercise.Custom) {
                Text(
                    text = state.exerciseError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Workout Parameters Section
            Text(
                text = stringResource(R.string.workout_parameters),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Weight and Reps in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.weight,
                    onValueChange = onWeightChanged,
                    label = { Text(stringResource(R.string.weight_label)) },
                    isError = state.weightError != null,
                    supportingText = state.weightError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = state.reps,
                    onValueChange = onRepsChanged,
                    label = { Text(stringResource(R.string.reps_label)) },
                    isError = state.repsError != null,
                    supportingText = state.repsError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Pause Time and Sets in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.pauseTime,
                    onValueChange = onPauseTimeChanged,
                    label = { Text(stringResource(R.string.pause_time_label)) },
                    isError = state.pauseTimeError != null,
                    supportingText = state.pauseTimeError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = state.sets,
                    onValueChange = onSetsChanged,
                    label = { Text(stringResource(R.string.sets_label)) },
                    isError = state.setsError != null,
                    supportingText = state.setsError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Start Button
            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.start_workout),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ExerciseCardsGrid(
    selectedExercise: Exercise?,
    onExerciseSelected: (Exercise) -> Unit
) {
    // First row: Deadlift and Bench Press
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExerciseCard(
            exercise = Exercise.Deadlift,
            isSelected = selectedExercise == Exercise.Deadlift,
            onClick = { onExerciseSelected(Exercise.Deadlift) },
            modifier = Modifier.weight(1f)
        )
        ExerciseCard(
            exercise = Exercise.BenchPress,
            isSelected = selectedExercise == Exercise.BenchPress,
            onClick = { onExerciseSelected(Exercise.BenchPress) },
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Second row: Rowing and Squat
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExerciseCard(
            exercise = Exercise.Rowing,
            isSelected = selectedExercise == Exercise.Rowing,
            onClick = { onExerciseSelected(Exercise.Rowing) },
            modifier = Modifier.weight(1f)
        )
        ExerciseCard(
            exercise = Exercise.Squat,
            isSelected = selectedExercise == Exercise.Squat,
            onClick = { onExerciseSelected(Exercise.Squat) },
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Third row: Custom Exercise
    ExerciseCard(
        exercise = Exercise.Custom(""),
        isSelected = selectedExercise is Exercise.Custom,
        onClick = { onExerciseSelected(Exercise.Custom("")) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    
    val borderWidth = if (isSelected) 2.dp else 1.dp
    
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        border = BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (exercise) {
                    is Exercise.Custom -> stringResource(R.string.exercise_custom)
                    else -> stringResource(exercise.nameResId)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
