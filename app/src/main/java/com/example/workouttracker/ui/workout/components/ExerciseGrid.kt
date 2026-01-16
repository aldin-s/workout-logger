package com.example.workouttracker.ui.workout.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.workouttracker.R
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.displayName
import org.burnoutcrew.reorderable.*

/**
 * Reorderable grid of exercise cards with X button for delete.
 * Drag anywhere to reorder, X to delete.
 */
@Composable
fun ExerciseGrid(
    exercises: List<Exercise>,
    selectedExercise: Exercise?,
    onExerciseClick: (Exercise) -> Unit,
    onDeleteClick: (Exercise) -> Unit,
    onAddClick: () -> Unit,
    onReorder: (List<Exercise>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state for drag operation - synced with external list
    var localExercises by remember { mutableStateOf(exercises) }
    var isDraggingActive by remember { mutableStateOf(false) }
    
    // Update local state when external list changes (but NOT during active drag)
    LaunchedEffect(exercises) {
        if (!isDraggingActive) {
            localExercises = exercises
        }
    }
    
    val state = rememberReorderableLazyGridState(
        onMove = { from, to ->
            // Only move if both indices are within exercises (not the add button)
            if (from.index < localExercises.size && to.index < localExercises.size) {
                isDraggingActive = true
                localExercises = localExercises.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                }
            }
        },
        onDragEnd = { _, _ ->
            // Persist the new order when drag ends
            onReorder(localExercises)
            isDraggingActive = false
        }
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = state.gridState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.reorderable(state)
    ) {
        items(localExercises, key = { it.id }) { exercise ->
            ReorderableItem(state, key = exercise.id) { isDragging ->
                ExerciseCardWithDelete(
                    exercise = exercise,
                    isSelected = selectedExercise?.id == exercise.id,
                    isDragging = isDragging,
                    onClick = { onExerciseClick(exercise) },
                    onDeleteClick = { onDeleteClick(exercise) },
                    modifier = Modifier.detectReorderAfterLongPress(state)
                )
            }
        }
        
        // Add exercise button (not reorderable)
        item {
            AddExerciseCard(onClick = onAddClick)
        }
    }
}

/**
 * Exercise card with X button in top-right corner.
 * Supports drag visual feedback.
 */
@Composable
private fun ExerciseCardWithDelete(
    exercise: Exercise,
    isSelected: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isDragging -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    
    val borderWidth = if (isSelected || isDragging) 2.dp else 1.dp
    val elevation = if (isDragging) 8.dp else 0.dp
    
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = elevation),
        colors = CardDefaults.outlinedCardColors(
            containerColor = when {
                isDragging -> MaterialTheme.colorScheme.surfaceVariant
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // X button in top-right corner
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Exercise name centered - uses displayName() for localized predefined exercises
            Text(
                text = exercise.displayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * Card for adding a new exercise.
 */
@Composable
private fun AddExerciseCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.add_exercise),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
