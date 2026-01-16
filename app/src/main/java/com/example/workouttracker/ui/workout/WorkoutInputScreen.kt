package com.example.workouttracker.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workouttracker.R
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.displayName
import com.example.workouttracker.ui.workout.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInputScreen(
    state: WorkoutInputState,
    exercises: List<Exercise>,
    onExerciseSelected: (Exercise) -> Unit,
    onWeightChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onPauseTimeChanged: (String) -> Unit,
    onSetsChanged: (String) -> Unit,
    onStartWorkout: () -> Unit,
    onNavigateBack: () -> Unit,
    onShowAddDialog: () -> Unit,
    onHideAddDialog: () -> Unit,
    onAddExercise: (String) -> Unit,
    onShowDeleteDialog: (Exercise) -> Unit,
    onHideDeleteDialog: () -> Unit,
    onDeleteExercise: (String) -> Unit,
    onReorderExercises: (List<Exercise>) -> Unit
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
                .imePadding()
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
            
            // Exercise Cards Grid - drag to reorder, X to delete
            ExerciseGrid(
                exercises = exercises,
                selectedExercise = state.selectedExercise,
                onExerciseClick = onExerciseSelected,
                onDeleteClick = onShowDeleteDialog,
                onAddClick = onShowAddDialog,
                onReorder = onReorderExercises,
                modifier = Modifier.heightIn(max = 400.dp)
            )
            
            // Exercise error
            if (state.exerciseError != null) {
                Text(
                    text = state.exerciseError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Workout Parameters Section - shows selected exercise name for context
            Text(
                text = state.selectedExercise?.let { exercise ->
                    "${exercise.displayName()} – ${stringResource(R.string.workout_parameters)}"
                } ?: stringResource(R.string.workout_parameters),
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
    
    // Dialogs
    if (state.showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = onHideAddDialog,
            onConfirm = onAddExercise,
            errorMessage = state.errorMessage
        )
    }
    
    state.showDeleteConfirmDialog?.let { exercise ->
        DeleteExerciseDialog(
            exerciseName = exercise.displayName(),
            onDismiss = onHideDeleteDialog,
            onConfirm = { onDeleteExercise(exercise.id) }
        )
    }
}
