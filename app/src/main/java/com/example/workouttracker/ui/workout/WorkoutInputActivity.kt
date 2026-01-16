package com.example.workouttracker.ui.workout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.workouttracker.ui.theme.RepsTheme
import com.example.workouttracker.ui.timer.TimerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutInputActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val viewModel: WorkoutInputViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val exercises by viewModel.exercises.collectAsState()
            
            RepsTheme(darkTheme = true, dynamicColor = false) {
                WorkoutInputScreen(
                    state = state,
                    exercises = exercises,
                    onExerciseSelected = viewModel::selectExercise,
                    onWeightChanged = viewModel::setWeight,
                    onRepsChanged = viewModel::setReps,
                    onPauseTimeChanged = viewModel::setPauseTime,
                    onSetsChanged = viewModel::setSets,
                    onStartWorkout = {
                        viewModel.validate()?.let { workoutData ->
                            startWorkout(workoutData)
                        }
                    },
                    onNavigateBack = { finish() },
                    onShowAddDialog = viewModel::showAddExerciseDialog,
                    onHideAddDialog = viewModel::hideAddExerciseDialog,
                    onAddExercise = viewModel::addExercise,
                    onShowDeleteDialog = viewModel::showDeleteConfirmDialog,
                    onHideDeleteDialog = viewModel::hideDeleteConfirmDialog,
                    onDeleteExercise = viewModel::deleteExercise,
                    onReorderExercises = viewModel::reorderExercises
                )
            }
        }
    }
    
    private fun startWorkout(data: WorkoutData) {
        val intent = Intent(this, TimerActivity::class.java).apply {
            putExtra("EXERCISE_NAME", data.exerciseName)
            putExtra("WEIGHT", data.weight)
            putExtra("REPS", data.reps)
            putExtra("PAUSE_TIME", data.pauseTime)
            putExtra("TOTAL_SETS", data.totalSets)
        }
        startActivity(intent)
        finish()
    }
}
