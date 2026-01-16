package com.example.workouttracker.ui.history

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.R
import com.example.workouttracker.ui.theme.RepsTheme

class WorkoutHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val historyViewModel: HistoryViewModel = viewModel()
            val uiState by historyViewModel.uiState.collectAsState()
            
            RepsTheme(darkTheme = true, dynamicColor = false) {
                val items = when (val state = uiState) {
                    is HistoryUiState.Loading -> emptyList()
                    is HistoryUiState.Success -> state.items
                    is HistoryUiState.Error -> {
                        showError(state.message)
                        emptyList()
                    }
                }
                
                HistoryScreen(
                    historyItems = items,
                    onNavigateBack = { finish() },
                    onSessionLongClick = { session ->
                        showActionDialog(session, historyViewModel)
                    }
                )
            }
        }
    }
    
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    private fun showActionDialog(session: WorkoutSession, viewModel: HistoryViewModel) {
        val options = arrayOf(
            getString(R.string.action_edit),
            getString(R.string.action_delete)
        )
        
        AlertDialog.Builder(this)
            .setTitle(R.string.workout_actions)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditDialog(session, viewModel)
                    1 -> showDeleteConfirmation(session, viewModel)
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
    
    private fun showEditDialog(session: WorkoutSession, viewModel: HistoryViewModel) {
        val bottomSheet = EditWorkoutBottomSheet(session) { weight, reps, sets ->
            viewModel.updateSession(session, weight, reps, sets)
            Toast.makeText(this, R.string.workout_updated, Toast.LENGTH_SHORT).show()
        }
        bottomSheet.show(supportFragmentManager, EditWorkoutBottomSheet.TAG)
    }
    
    private fun showDeleteConfirmation(session: WorkoutSession, viewModel: HistoryViewModel) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_workout_title)
            .setMessage(getString(R.string.delete_workout_message, session.exerciseName, session.totalSets))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteSession(session)
                Toast.makeText(this, R.string.workout_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}
