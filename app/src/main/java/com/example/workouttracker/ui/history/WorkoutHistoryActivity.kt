package com.example.workouttracker.ui.history

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.R
import com.example.workouttracker.ui.theme.RepsTheme
import kotlinx.coroutines.launch

class WorkoutHistoryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val historyViewModel: HistoryViewModel = viewModel()
            val uiState by historyViewModel.uiState.collectAsState()
            
            RepsTheme(darkTheme = true, dynamicColor = false) {
                HistoryScreenWithDialogs(
                    uiState = uiState,
                    viewModel = historyViewModel,
                    onNavigateBack = { finish() },
                    onShowToast = { messageResId ->
                        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreenWithDialogs(
    uiState: HistoryUiState,
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onShowToast: (Int) -> Unit
) {
    var selectedSession by remember { mutableStateOf<WorkoutSession?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    val items = when (uiState) {
        is HistoryUiState.Loading -> emptyList()
        is HistoryUiState.Success -> uiState.items
        is HistoryUiState.Error -> {
            showErrorDialog = uiState.message
            emptyList()
        }
    }
    
    HistoryScreen(
        historyItems = items,
        onNavigateBack = onNavigateBack,
        onSessionLongClick = { session ->
            selectedSession = session
            showActionDialog = true
        }
    )
    
    // Action Dialog (Edit/Delete)
    if (showActionDialog && selectedSession != null) {
        ActionDialog(
            onDismiss = { showActionDialog = false },
            onEdit = {
                showActionDialog = false
                showEditSheet = true
            },
            onDelete = {
                showActionDialog = false
                showDeleteDialog = true
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedSession != null) {
        DeleteConfirmationDialog(
            session = selectedSession!!,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteSession(selectedSession!!)
                showDeleteDialog = false
                selectedSession = null
                onShowToast(R.string.workout_deleted)
            }
        )
    }
    
    // Edit Bottom Sheet
    if (showEditSheet && selectedSession != null) {
        EditWorkoutSheet(
            session = selectedSession!!,
            sheetState = sheetState,
            onDismiss = {
                scope.launch { sheetState.hide() }
                showEditSheet = false
                selectedSession = null
            },
            onSave = { weight, reps, sets ->
                viewModel.updateSession(selectedSession!!, weight, reps, sets)
                scope.launch { sheetState.hide() }
                showEditSheet = false
                selectedSession = null
                onShowToast(R.string.workout_updated)
            }
        )
    }
    
    // Error Dialog
    showErrorDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text(stringResource(R.string.error_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun ActionDialog(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workout_actions)) },
        text = null,
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text(stringResource(R.string.action_edit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.action_delete))
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    session: WorkoutSession,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_workout_title)) },
        text = { 
            Text(stringResource(R.string.delete_workout_message, session.exerciseName, session.totalSets))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
