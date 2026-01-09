package com.asstudio.berlin.reps.ui.workout

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.asstudio.berlin.reps.R
import com.asstudio.berlin.reps.data.database.WorkoutDatabase
import com.asstudio.berlin.reps.data.model.CustomExercise
import com.asstudio.berlin.reps.ui.timer.TimerActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class WorkoutInputActivity : AppCompatActivity() {
    
    private lateinit var database: WorkoutDatabase
    
    private lateinit var cardKreuzheben: MaterialCardView
    private lateinit var cardBankdruecken: MaterialCardView
    private lateinit var cardRudern: MaterialCardView
    private lateinit var cardKniebeuge: MaterialCardView
    private lateinit var newExerciseButton: MaterialButton
    
    private lateinit var deleteKreuzheben: ImageButton
    private lateinit var deleteBankdruecken: ImageButton
    private lateinit var deleteRudern: ImageButton
    private lateinit var deleteKniebeuge: ImageButton
    
    private lateinit var recentlyUsedHeader: TextView
    private lateinit var recentlyUsedRecyclerView: RecyclerView
    private lateinit var customExercisesRecyclerView: RecyclerView
    private lateinit var recentAdapter: RecentExerciseAdapter
    private lateinit var customAdapter: CustomExerciseAdapter
    
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
        setupExerciseCards()
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
                getString(R.string.exercise_deadlift),
                getString(R.string.exercise_bench_press),
                getString(R.string.exercise_rowing),
                getString(R.string.exercise_squat)
            )
            
            standardExercises.forEach { name ->
                val existing = database.customExerciseDao().getExerciseByName(name)
                if (existing == null) {
                    database.customExerciseDao().insertExercise(
                        CustomExercise(
                            name = name,
                            createdAt = 0,
                            lastUsed = 0
                        )
                    )
                }
            }
        }
    }

    private fun initViews() {
        cardKreuzheben = findViewById(R.id.cardKreuzheben)
        cardBankdruecken = findViewById(R.id.cardBankdruecken)
        cardRudern = findViewById(R.id.cardRudern)
        cardKniebeuge = findViewById(R.id.cardKniebeuge)
        newExerciseButton = findViewById(R.id.newExerciseButton)
        
        deleteKreuzheben = findViewById(R.id.deleteKreuzheben)
        deleteBankdruecken = findViewById(R.id.deleteBankdruecken)
        deleteRudern = findViewById(R.id.deleteRudern)
        deleteKniebeuge = findViewById(R.id.deleteKniebeuge)
        
        recentlyUsedHeader = findViewById(R.id.recentlyUsedHeader)
        recentlyUsedRecyclerView = findViewById(R.id.recentlyUsedRecyclerView)
        customExercisesRecyclerView = findViewById(R.id.customExercisesRecyclerView)
        
        selectedExerciseLabel = findViewById(R.id.selectedExerciseLabel)
        selectedExerciseText = findViewById(R.id.selectedExerciseText)
        
        customExerciseLayout = findViewById(R.id.customExerciseLayout)
        customExerciseEditText = findViewById(R.id.customExerciseEditText)
        
        weightEditText = findViewById(R.id.weightEditText)
        repsEditText = findViewById(R.id.repsEditText)
        pauseTimeEditText = findViewById(R.id.pauseTimeEditText)
        setsEditText = findViewById(R.id.setsEditText)
        startButton = findViewById(R.id.startButton)
        
        deleteKreuzheben.setOnClickListener {
            deleteCustomExercise(getString(R.string.exercise_deadlift))
        }
        deleteBankdruecken.setOnClickListener {
            deleteCustomExercise(getString(R.string.exercise_bench_press))
        }
        deleteRudern.setOnClickListener {
            deleteCustomExercise(getString(R.string.exercise_rowing))
        }
        deleteKniebeuge.setOnClickListener {
            deleteCustomExercise(getString(R.string.exercise_squat))
        }
    }
    
    private fun setupRecyclerViews() {
        recentAdapter = RecentExerciseAdapter(
            onExerciseClick = { exerciseName ->
                selectExercise(exerciseName, false)
            },
            onDeleteClick = { exerciseName ->
                deleteCustomExercise(exerciseName)
            }
        )
        
        recentlyUsedRecyclerView.apply {
            adapter = recentAdapter
            layoutManager = LinearLayoutManager(
                this@WorkoutInputActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
        
        customAdapter = CustomExerciseAdapter(
            onExerciseClick = { exerciseName ->
                selectExercise(exerciseName, true)
            },
            onDeleteClick = { exercise ->
                deleteCustomExercise(exercise.name)
            }
        )
        
        customExercisesRecyclerView.apply {
            adapter = customAdapter
            layoutManager = GridLayoutManager(this@WorkoutInputActivity, 2)
        }
    }
    
    private fun loadExercises() {
        var totalVisibleExercises = 0
        
        lifecycleScope.launch {
            database.customExerciseDao().getAllVisibleExercises().collect { visibleExercises ->
                totalVisibleExercises = visibleExercises.size
                val visibleNames = visibleExercises.map { it.name }.toSet()
                
                cardKreuzheben.visibility = if (visibleNames.contains(getString(R.string.exercise_deadlift))) View.VISIBLE else View.GONE
                cardBankdruecken.visibility = if (visibleNames.contains(getString(R.string.exercise_bench_press))) View.VISIBLE else View.GONE
                cardRudern.visibility = if (visibleNames.contains(getString(R.string.exercise_rowing))) View.VISIBLE else View.GONE
                cardKniebeuge.visibility = if (visibleNames.contains(getString(R.string.exercise_squat))) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            database.customExerciseDao().getRecentlyUsed(10).collect { exercises ->
                recentAdapter.submitList(exercises.map { 
                    RecentExercise(it.name, it.createdAt != 0L)
                })
                
                // Nur anzeigen wenn mehr als 8 Übungen UND mindestens eine wurde verwendet
                val shouldShow = totalVisibleExercises > 8 && exercises.isNotEmpty()
                recentlyUsedHeader.visibility = if (shouldShow) View.VISIBLE else View.GONE
                recentlyUsedRecyclerView.visibility = if (shouldShow) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            database.customExerciseDao().getAllCustomExercises().collect { exercises ->
                customAdapter.submitList(exercises)
            }
        }
    }
    
    private fun setupExerciseCards() {
        cardKreuzheben.setOnClickListener {
            selectExercise(getString(R.string.exercise_deadlift), false)
        }
        
        cardBankdruecken.setOnClickListener {
            selectExercise(getString(R.string.exercise_bench_press), false)
        }
        
        cardRudern.setOnClickListener {
            selectExercise(getString(R.string.exercise_rowing), false)
        }
        
        cardKniebeuge.setOnClickListener {
            selectExercise(getString(R.string.exercise_squat), false)
        }
        
        newExerciseButton.setOnClickListener {
            selectExercise("", true)
        }
    }
    
    private fun selectExercise(exercise: String, custom: Boolean) {
        selectedExercise = exercise
        isCustomExercise = custom
        
        resetCardBorders()
        
        if (custom && exercise.isEmpty()) {
            // Neue Übung: Textfeld anzeigen
            customExerciseLayout.visibility = View.VISIBLE
            selectedExerciseLabel.visibility = View.GONE
            selectedExerciseText.visibility = View.GONE
        } else if (custom) {
            // Bestehende Custom Exercise: Name anzeigen
            customExerciseLayout.visibility = View.GONE
            selectedExerciseLabel.visibility = View.VISIBLE
            selectedExerciseText.visibility = View.VISIBLE
            selectedExerciseText.text = exercise
        } else {
            // Standard Exercise: Name anzeigen
            customExerciseLayout.visibility = View.GONE
            selectedExerciseLabel.visibility = View.VISIBLE
            selectedExerciseText.visibility = View.VISIBLE
            selectedExerciseText.text = exercise
            
            when (exercise) {
                getString(R.string.exercise_deadlift) -> {
                    cardKreuzheben.setStrokeColor(ContextCompat.getColor(this, R.color.gray_900))
                    cardKreuzheben.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.card_stroke_selected))
                }
                getString(R.string.exercise_bench_press) -> {
                    cardBankdruecken.setStrokeColor(ContextCompat.getColor(this, R.color.gray_900))
                    cardBankdruecken.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.card_stroke_selected))
                }
                getString(R.string.exercise_rowing) -> {
                    cardRudern.setStrokeColor(ContextCompat.getColor(this, R.color.gray_900))
                    cardRudern.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.card_stroke_selected))
                }
                getString(R.string.exercise_squat) -> {
                    cardKniebeuge.setStrokeColor(ContextCompat.getColor(this, R.color.gray_900))
                    cardKniebeuge.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.card_stroke_selected))
                }
            }
        }
    }
    
    private fun resetCardBorders() {
        val defaultStrokeColor = ContextCompat.getColor(this, R.color.gray_200)
        val defaultStrokeWidth = resources.getDimensionPixelSize(R.dimen.card_stroke_default)
        
        cardKreuzheben.setStrokeColor(defaultStrokeColor)
        cardKreuzheben.setStrokeWidth(defaultStrokeWidth)
        cardBankdruecken.setStrokeColor(defaultStrokeColor)
        cardBankdruecken.setStrokeWidth(defaultStrokeWidth)
        cardRudern.setStrokeColor(defaultStrokeColor)
        cardRudern.setStrokeWidth(defaultStrokeWidth)
        cardKniebeuge.setStrokeColor(defaultStrokeColor)
        cardKniebeuge.setStrokeWidth(defaultStrokeWidth)
    }
    
    private fun setupClickListeners() {
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
                database.customExerciseDao().insertExercise(
                    CustomExercise(
                        name = exerciseName,
                        createdAt = currentTime,
                        lastUsed = currentTime,
                        usageCount = 1
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
            finish()
        }
    }
}
