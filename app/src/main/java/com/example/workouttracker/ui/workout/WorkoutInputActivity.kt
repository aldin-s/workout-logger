package com.example.workouttracker.ui.workout

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.workouttracker.R
import com.example.workouttracker.ui.timer.TimerActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class WorkoutInputActivity : AppCompatActivity() {
    
    // Exercise selection cards
    private lateinit var cardKreuzheben: CardView
    private lateinit var cardBankdruecken: CardView
    private lateinit var cardRudern: CardView
    private lateinit var cardKniebeuge: CardView
    private lateinit var cardCustomExercise: CardView
    
    // Selected exercise display
    private lateinit var selectedExerciseLabel: TextView
    private lateinit var selectedExerciseText: TextView
    
    // Custom exercise input
    private lateinit var customExerciseLayout: TextInputLayout
    private lateinit var customExerciseEditText: TextInputEditText
    
    // Workout parameters
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

        initViews()
        setupExerciseCards()
        setupClickListeners()
        setDefaultValues()
    }

    private fun initViews() {
        // Exercise cards
        cardKreuzheben = findViewById(R.id.cardKreuzheben)
        cardBankdruecken = findViewById(R.id.cardBankdruecken)
        cardRudern = findViewById(R.id.cardRudern)
        cardKniebeuge = findViewById(R.id.cardKniebeuge)
        cardCustomExercise = findViewById(R.id.cardCustomExercise)
        
        // Selected exercise display
        selectedExerciseLabel = findViewById(R.id.selectedExerciseLabel)
        selectedExerciseText = findViewById(R.id.selectedExerciseText)
        
        // Custom exercise input
        customExerciseLayout = findViewById(R.id.customExerciseLayout)
        customExerciseEditText = findViewById(R.id.customExerciseEditText)
        
        // Workout parameters
        weightEditText = findViewById(R.id.weightEditText)
        repsEditText = findViewById(R.id.repsEditText)
        pauseTimeEditText = findViewById(R.id.pauseTimeEditText)
        setsEditText = findViewById(R.id.setsEditText)
        startButton = findViewById(R.id.startButton)
    }
    
    private fun setupExerciseCards() {
        cardKreuzheben.setOnClickListener {
            selectExercise("Kreuzheben", false)
        }
        
        cardBankdruecken.setOnClickListener {
            selectExercise("Bankdrücken", false)
        }
        
        cardRudern.setOnClickListener {
            selectExercise("Rudern", false)
        }
        
        cardKniebeuge.setOnClickListener {
            selectExercise("Kniebeuge", false)
        }
        
        cardCustomExercise.setOnClickListener {
            selectExercise("", true)
        }
    }
    
    private fun selectExercise(exercise: String, custom: Boolean) {
        selectedExercise = exercise
        isCustomExercise = custom
        
        // Reset all card backgrounds
        resetCardBackgrounds()
        
        if (custom) {
            // Show custom input
            customExerciseLayout.visibility = View.VISIBLE
            selectedExerciseLabel.visibility = View.GONE
            selectedExerciseText.visibility = View.GONE
            cardCustomExercise.setCardBackgroundColor(getColor(R.color.exercise_selected))
        } else {
            // Show selected exercise
            customExerciseLayout.visibility = View.GONE
            selectedExerciseLabel.visibility = View.VISIBLE
            selectedExerciseText.visibility = View.VISIBLE
            selectedExerciseText.text = exercise
            
            // Highlight selected card
            when (exercise) {
                "Kreuzheben" -> cardKreuzheben.setCardBackgroundColor(getColor(R.color.exercise_selected))
                "Bankdrücken" -> cardBankdruecken.setCardBackgroundColor(getColor(R.color.exercise_selected))
                "Rudern" -> cardRudern.setCardBackgroundColor(getColor(R.color.exercise_selected))
                "Kniebeuge" -> cardKniebeuge.setCardBackgroundColor(getColor(R.color.exercise_selected))
            }
        }
    }
    
    private fun resetCardBackgrounds() {
        val defaultColor = getColor(android.R.color.white)
        cardKreuzheben.setCardBackgroundColor(defaultColor)
        cardBankdruecken.setCardBackgroundColor(defaultColor)
        cardRudern.setCardBackgroundColor(defaultColor)
        cardKniebeuge.setCardBackgroundColor(defaultColor)
        cardCustomExercise.setCardBackgroundColor(getColor(R.color.exercise_custom_bg))
    }

    private fun setupClickListeners() {
        startButton.setOnClickListener {
            if (validateInput()) {
                startWorkout()
            }
        }
    }


    private fun setDefaultValues() {
        pauseTimeEditText.setText("60") // Default 60 seconds pause
        setsEditText.setText("3") // Default 3 sets
    }

    private fun validateInput(): Boolean {
        // Get exercise name
        val exerciseName = if (isCustomExercise) {
            customExerciseEditText.text.toString().trim()
        } else {
            selectedExercise
        }
        
        val weightText = weightEditText.text.toString().trim()
        val repsText = repsEditText.text.toString().trim()
        val pauseTimeText = pauseTimeEditText.text.toString().trim()
        val setsText = setsEditText.text.toString().trim()

        when {
            exerciseName.isEmpty() -> {
                if (isCustomExercise) {
                    customExerciseEditText.error = "Übung eingeben"
                } else {
                    Toast.makeText(this, "Bitte wähle eine Übung", Toast.LENGTH_SHORT).show()
                }
                return false
            }
            weightText.isEmpty() -> {
                weightEditText.error = "Gewicht eingeben"
                return false
            }
            repsText.isEmpty() -> {
                repsEditText.error = "Wiederholungen eingeben"
                return false
            }
            pauseTimeText.isEmpty() -> {
                pauseTimeEditText.error = "Pausenzeit eingeben"
                return false
            }
            setsText.isEmpty() -> {
                setsEditText.error = "Anzahl Sätze eingeben"
                return false
            }
        }

        return true
    }

    private fun startWorkout() {
        val exerciseName = if (isCustomExercise) {
            customExerciseEditText.text.toString().trim()
        } else {
            selectedExercise
        }
        
        val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0
        val reps = repsEditText.text.toString().toIntOrNull() ?: 0
        val pauseTime = pauseTimeEditText.text.toString().toIntOrNull() ?: 60
        val totalSets = setsEditText.text.toString().toIntOrNull() ?: 1

        val intent = Intent(this, TimerActivity::class.java).apply {
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