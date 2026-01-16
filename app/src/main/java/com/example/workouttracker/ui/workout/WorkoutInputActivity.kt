package com.example.workouttracker.ui.workout

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.ui.theme.RepsTheme
import com.example.workouttracker.ui.timer.TimerActivity

class WorkoutInputActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val workoutViewModel: WorkoutInputViewModel = viewModel()
            val state by workoutViewModel.state.collectAsState()
            
            RepsTheme(darkTheme = true, dynamicColor = false) {
                WorkoutInputScreen(
                    state = state,
                    onExerciseSelected = workoutViewModel::selectExercise,
                    onCustomExerciseNameChanged = workoutViewModel::setCustomExerciseName,
                    onWeightChanged = workoutViewModel::setWeight,
                    onRepsChanged = workoutViewModel::setReps,
                    onPauseTimeChanged = workoutViewModel::setPauseTime,
                    onSetsChanged = workoutViewModel::setSets,
                    onStartWorkout = {
                        workoutViewModel.validate()?.let { workoutData ->
                            startWorkout(workoutData)
                        }
                    },
                    onNavigateBack = { finish() }
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
