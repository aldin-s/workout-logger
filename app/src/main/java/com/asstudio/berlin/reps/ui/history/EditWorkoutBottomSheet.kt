package com.asstudio.berlin.reps.ui.history

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.asstudio.berlin.reps.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Locale

class EditWorkoutBottomSheet(
    private val session: WorkoutSession,
    private val onSave: (weight: Double, reps: Int, sets: Int) -> Unit
) : BottomSheetDialogFragment() {
    
    private lateinit var exerciseNameText: TextView
    private lateinit var workoutInfoText: TextView
    private lateinit var weightInputLayout: TextInputLayout
    private lateinit var weightInput: TextInputEditText
    private lateinit var repsInputLayout: TextInputLayout
    private lateinit var repsInput: TextInputEditText
    private lateinit var setsInputLayout: TextInputLayout
    private lateinit var setsInput: TextInputEditText
    private lateinit var volumeText: TextView
    private lateinit var changeIndicator: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    
    private val originalVolume: Double by lazy {
        session.weight * session.reps * session.totalSets
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_edit_workout, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        updateVolumeCalculation()
    }
    
    private fun initViews(view: View) {
        exerciseNameText = view.findViewById(R.id.exerciseNameText)
        workoutInfoText = view.findViewById(R.id.workoutInfoText)
        weightInputLayout = view.findViewById(R.id.weightInputLayout)
        weightInput = view.findViewById(R.id.editWeight)
        repsInputLayout = view.findViewById(R.id.repsInputLayout)
        repsInput = view.findViewById(R.id.editReps)
        setsInputLayout = view.findViewById(R.id.setsInputLayout)
        setsInput = view.findViewById(R.id.editSets)
        volumeText = view.findViewById(R.id.volumeText)
        changeIndicator = view.findViewById(R.id.changeIndicator)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        
        // Set header info
        exerciseNameText.text = session.exerciseName.uppercase()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        workoutInfoText.text = "${session.totalSets} ${getString(R.string.sets_label)} · ${timeFormat.format(session.startTime)}"
        
        // Pre-fill values
        weightInput.setText(session.weight.toString())
        repsInput.setText(session.reps.toString())
        setsInput.setText(session.totalSets.toString())
        
        // Set helper texts (show original values)
        weightInputLayout.helperText = getString(R.string.edit_original_value, session.weight.toString() + " kg")
        repsInputLayout.helperText = getString(R.string.edit_original_value, session.reps.toString())
        setsInputLayout.helperText = getString(R.string.edit_original_value, session.totalSets.toString())
    }
    
    private fun setupListeners() {
        // Live calculation on text change
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateVolumeCalculation()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        
        weightInput.addTextChangedListener(textWatcher)
        repsInput.addTextChangedListener(textWatcher)
        setsInput.addTextChangedListener(textWatcher)
        
        saveButton.setOnClickListener {
            saveChanges()
        }
        
        cancelButton.setOnClickListener {
            dismiss()
        }
    }
    
    private fun updateVolumeCalculation() {
        val weight = weightInput.text.toString().toDoubleOrNull() ?: 0.0
        val reps = repsInput.text.toString().toIntOrNull() ?: 0
        val sets = setsInput.text.toString().toIntOrNull() ?: 0
        
        val newVolume = weight * reps * sets
        volumeText.text = String.format("%.0f kg", newVolume)
        
        // Update change indicator
        val difference = newVolume - originalVolume
        when {
            difference > 0 -> {
                volumeText.setTextColor(Color.parseColor("#90EE90")) // Green
                changeIndicator.text = getString(R.string.edit_volume_increase, String.format("%.0f", difference))
                changeIndicator.setTextColor(Color.parseColor("#90EE90"))
                changeIndicator.visibility = View.VISIBLE
            }
            difference < 0 -> {
                volumeText.setTextColor(Color.parseColor("#FFB74D")) // Orange
                changeIndicator.text = getString(R.string.edit_volume_decrease, String.format("%.0f", Math.abs(difference)))
                changeIndicator.setTextColor(Color.parseColor("#FFB74D"))
                changeIndicator.visibility = View.VISIBLE
            }
            else -> {
                volumeText.setTextColor(Color.WHITE)
                changeIndicator.visibility = View.GONE
            }
        }
        
        // Clear errors when user types
        weightInputLayout.error = null
        repsInputLayout.error = null
        setsInputLayout.error = null
    }
    
    private fun saveChanges() {
        val weight = weightInput.text.toString().toDoubleOrNull()
        val reps = repsInput.text.toString().toIntOrNull()
        val sets = setsInput.text.toString().toIntOrNull()
        
        var hasError = false
        
        // Validation
        if (weight == null || weight <= 0) {
            weightInputLayout.error = getString(R.string.error_weight_invalid)
            hasError = true
        }
        
        if (reps == null || reps <= 0) {
            repsInputLayout.error = getString(R.string.error_reps_invalid)
            hasError = true
        }
        
        if (sets == null || sets <= 0) {
            setsInputLayout.error = getString(R.string.error_sets_invalid)
            hasError = true
        }
        
        if (!hasError) {
            onSave(weight!!, reps!!, sets!!)
            dismiss()
        }
    }
    
    companion object {
        const val TAG = "EditWorkoutBottomSheet"
    }
}
