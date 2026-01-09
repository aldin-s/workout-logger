package com.asstudio.berlin.reps.ui.workout

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.asstudio.berlin.reps.R
import com.asstudio.berlin.reps.data.database.WorkoutDatabase
import com.asstudio.berlin.reps.data.model.CustomExercise
import com.asstudio.berlin.reps.ui.timer.TimerActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class WorkoutInputActivity : AppCompatActivity() {
    
    private lateinit var database: WorkoutDatabase
    
    private lateinit var newExerciseButton: MaterialButton
    
    private lateinit var allExercisesRecyclerView: RecyclerView
    private lateinit var allExercisesAdapter: DraggableExerciseAdapter
    
    private lateinit var selectedExerciseLabel: TextView
    private lateinit var selectedExerciseText: TextView
    
    private lateinit var customExerciseLayout: TextInputLayout
    private lateinit var customExerciseEditText: TextInputEditText
    
    private lateinit var weightEditText: TextInputEditText
    private lateinit var repsEditText: TextInputEditText
    private lateinit var pauseTimeEditText: TextInputEditText
    private lateinit var setsEditText: TextInputEditText
    private lateinit var startButton: Button
    
    private var selectedExercise: String = ""
    private var isCustomExercise: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_input)

        database = WorkoutDatabase.getDatabase(this)
        
        initViews()
        setupRecyclerViews()
        initializeStandardExercises()
        setupClickListeners()
        setDefaultValues()
        loadExercises()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload exercises when returning from timer
        loadExercises()
    }
    
    private fun initializeStandardExercises() {
        lifecycleScope.launch {
            val standardExercises = listOf(
                getString(R.string.exercise_deadlift) to 1,
                getString(R.string.exercise_bench_press) to 2,
                getString(R.string.exercise_rowing) to 3,
                getString(R.string.exercise_squat) to 4
            )
            
            standardExercises.forEach { (name, order) ->
                val existing = database.customExerciseDao().getExerciseByName(name)
                if (existing == null) {
                    database.customExerciseDao().insertExercise(
                        CustomExercise(
                            name = name,
                            createdAt = 0,
                            lastUsed = 0,
                            sortOrder = order
                        )
                    )
                }
            }
        }
    }

    private fun initViews() {
        newExerciseButton = findViewById(R.id.newExerciseButton)
        allExercisesRecyclerView = findViewById(R.id.allExercisesRecyclerView)
        
        selectedExerciseLabel = findViewById(R.id.selectedExerciseLabel)
        selectedExerciseText = findViewById(R.id.selectedExerciseText)
        
        customExerciseLayout = findViewById(R.id.customExerciseLayout)
        customExerciseEditText = findViewById(R.id.customExerciseEditText)
        
        weightEditText = findViewById(R.id.weightEditText)
        repsEditText = findViewById(R.id.repsEditText)
        pauseTimeEditText = findViewById(R.id.pauseTimeEditText)
        setsEditText = findViewById(R.id.setsEditText)
        startButton = findViewById(R.id.startButton)
    }
    
    private fun setupRecyclerViews() {
        // All exercises draggable grid
        allExercisesAdapter = DraggableExerciseAdapter(
            onExerciseClick = { exercise ->
                selectExercise(exercise.name, exercise.createdAt != 0L)
            },
            onDeleteClick = { exercise ->
                deleteCustomExercise(exercise.name)
            },
            onItemMoved = { updatedList ->
                saveExerciseOrder(updatedList)
            }
        )
        
        allExercisesRecyclerView.apply {
            adapter = allExercisesAdapter
            layoutManager = GridLayoutManager(this@WorkoutInputActivity, 2)
            
            // Attach ItemTouchHelper for drag & drop
            val touchHelper = ItemTouchHelper(ExerciseItemTouchHelper(allExercisesAdapter))
            touchHelper.attachToRecyclerView(this)
        }
    }
    
    private fun saveExerciseOrder(exercises: List<CustomExercise>) {
        lifecycleScope.launch {
            database.customExerciseDao().updateAllSortOrders(exercises)
        }
    }
    
    private fun loadExercises() {
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            // INSTANT: Load once immediately
            val exercises = database.customExerciseDao().getAllVisibleExercisesOnce()
            allExercisesAdapter.submitList(exercises)
        }
        
        // THEN: Flow for live updates
        lifecycleScope.launch {
            database.customExerciseDao().getAllVisibleExercises().collect { exercises ->
                allExercisesAdapter.submitList(exercises)
            }
        }
    }
    
    private fun selectExercise(exercise: String, custom: Boolean) {
        selectedExercise = exercise
        isCustomExercise = custom
        
        if (custom && exercise.isEmpty()) {
            // Neue Übung: Textfeld anzeigen
            customExerciseLayout.visibility = View.VISIBLE
            selectedExerciseLabel.visibility = View.GONE
            selectedExerciseText.visibility = View.GONE
        } else {
            // Bestehende Übung (Standard oder Custom): Name anzeigen
            customExerciseLayout.visibility = View.GONE
            selectedExerciseLabel.visibility = View.VISIBLE
            selectedExerciseText.visibility = View.VISIBLE
            selectedExerciseText.text = exercise
        }
    }
    
    private fun setupClickListeners() {
        newExerciseButton.setOnClickListener {
            selectExercise("", true)
        }
        
        startButton.setOnClickListener {
            if (validateInput()) {
                startWorkout()
            }
        }
    }
    
    private fun setDefaultValues() {
        pauseTimeEditText.setText("120")
        setsEditText.setText("5")
    }
    
    private fun deleteCustomExercise(exerciseName: String) {
        lifecycleScope.launch {
            database.customExerciseDao().hideExercise(exerciseName)
            Toast.makeText(this@WorkoutInputActivity, 
                getString(R.string.delete_exercise) + ": $exerciseName", 
                Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun validateInput(): Boolean {
        val exerciseName = if (isCustomExercise && selectedExercise.isEmpty()) {
            // Neue Custom Exercise: Text aus Eingabefeld
            customExerciseEditText.text.toString().trim()
        } else {
            // Standard Exercise oder bestehende Custom Exercise
            selectedExercise
        }
        
        val weightText = weightEditText.text.toString().trim()
        val repsText = repsEditText.text.toString().trim()
        val pauseTimeText = pauseTimeEditText.text.toString().trim()
        val setsText = setsEditText.text.toString().trim()

        when {
            exerciseName.isEmpty() -> {
                if (isCustomExercise && selectedExercise.isEmpty()) {
                    customExerciseEditText.error = getString(R.string.error_enter_exercise)
                } else {
                    Toast.makeText(this, getString(R.string.error_select_exercise), Toast.LENGTH_SHORT).show()
                }
                return false
            }
            weightText.isEmpty() -> {
                weightEditText.error = getString(R.string.error_enter_weight)
                return false
            }
            repsText.isEmpty() -> {
                repsEditText.error = getString(R.string.error_enter_reps)
                return false
            }
            pauseTimeText.isEmpty() -> {
                pauseTimeEditText.error = getString(R.string.error_enter_pause)
                return false
            }
            setsText.isEmpty() -> {
                setsEditText.error = getString(R.string.error_enter_sets)
                return false
            }
        }
        
        val weight = weightText.toDoubleOrNull()
        val reps = repsText.toIntOrNull()
        val pauseTime = pauseTimeText.toIntOrNull()
        val sets = setsText.toIntOrNull()
        
        when {
            weight == null || weight <= 0 -> {
                weightEditText.error = getString(R.string.error_weight_invalid)
                weightEditText.requestFocus()
                return false
            }
            reps == null || reps <= 0 -> {
                repsEditText.error = getString(R.string.error_reps_invalid)
                repsEditText.requestFocus()
                return false
            }
            pauseTime == null || pauseTime <= 0 -> {
                pauseTimeEditText.error = getString(R.string.error_pause_invalid)
                pauseTimeEditText.requestFocus()
                return false
            }
            sets == null || sets <= 0 -> {
                setsEditText.error = getString(R.string.error_sets_invalid)
                setsEditText.requestFocus()
                return false
            }
        }

        return true
    }

    private fun startWorkout() {
        val exerciseName = if (isCustomExercise && selectedExercise.isEmpty()) {
            // Neue Custom Exercise: Text aus Eingabefeld
            customExerciseEditText.text.toString().trim()
        } else {
            // Standard Exercise oder bestehende Custom Exercise
            selectedExercise
        }
        
        val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0
        val reps = repsEditText.text.toString().toIntOrNull() ?: 0
        val pauseTime = pauseTimeEditText.text.toString().toIntOrNull() ?: 60
        val totalSets = setsEditText.text.toString().toIntOrNull() ?: 1

        // Save exercise BEFORE starting intent
        lifecycleScope.launch {
            val currentTime = System.currentTimeMillis()
            val existing = database.customExerciseDao().getExerciseByName(exerciseName)
            
            if (existing != null) {
                database.customExerciseDao().updateUsage(exerciseName, currentTime)
            } else {
                // New custom exercise - add after last exercise
                val maxSortOrder = database.customExerciseDao().getMaxSortOrder() ?: 0
                database.customExerciseDao().insertExercise(
                    CustomExercise(
                        name = exerciseName,
                        createdAt = currentTime,
                        lastUsed = currentTime,
                        usageCount = 1,
                        sortOrder = maxSortOrder + 1
                    )
                )
            }
            
            // Start timer activity AFTER saving
            val intent = Intent(this@WorkoutInputActivity, TimerActivity::class.java).apply {
                putExtra("EXERCISE_NAME", exerciseName)
                putExtra("WEIGHT", weight)
                putExtra("REPS", reps)
                putExtra("PAUSE_TIME", pauseTime)
                putExtra("TOTAL_SETS", totalSets)
            }
            
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}
