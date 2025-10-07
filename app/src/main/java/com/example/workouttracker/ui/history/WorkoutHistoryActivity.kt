package com.example.workouttracker.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workouttracker.R
import com.example.workouttracker.data.database.WorkoutDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WorkoutHistoryAdapter
    private lateinit var database: WorkoutDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_history)

        initViews()
        setupRecyclerView()
        loadWorkoutHistory()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.historyRecyclerView)
        database = WorkoutDatabase.getDatabase(this)
    }

    private fun setupRecyclerView() {
        adapter = WorkoutHistoryAdapter(
            onItemLongClick = { session ->
                showActionDialog(session)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadWorkoutHistory() {
        lifecycleScope.launch {
            val completedSets = database.completedSetDao().getAllSets()
            val groupedItems = HistoryGrouper.groupByDate(completedSets, this@WorkoutHistoryActivity)
            adapter.submitList(groupedItems)
        }
    }
    
    private fun showActionDialog(session: WorkoutSession) {
        val options = arrayOf(
            getString(R.string.action_edit),
            getString(R.string.action_delete)
        )
        
        AlertDialog.Builder(this)
            .setTitle(R.string.workout_actions)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditDialog(session)
                    1 -> showDeleteConfirmation(session)
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
    
    private fun showEditDialog(session: WorkoutSession) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_workout, null)
        
        val weightInput = dialogView.findViewById<EditText>(R.id.editWeight)
        val repsInput = dialogView.findViewById<EditText>(R.id.editReps)
        val infoText = dialogView.findViewById<TextView>(R.id.infoText)
        
        // Pre-fill with current values
        weightInput.setText(session.weight.toString())
        repsInput.setText(session.reps.toString())
        
        // Info (read-only)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        infoText.text = "${session.exerciseName.uppercase()}\n" +
                "${session.totalSets} ${getString(R.string.sets_label)} · ${timeFormat.format(session.startTime)}"
        
        AlertDialog.Builder(this)
            .setTitle(R.string.edit_workout)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val newWeight = weightInput.text.toString().toDoubleOrNull()
                val newReps = repsInput.text.toString().toIntOrNull()
                
                if (newWeight != null && newReps != null && newWeight > 0 && newReps > 0) {
                    updateSession(session, newWeight, newReps)
                } else {
                    Toast.makeText(this, R.string.error_invalid_input, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
    
    private fun showDeleteConfirmation(session: WorkoutSession) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_workout_title)
            .setMessage(getString(R.string.delete_workout_message, session.exerciseName, session.totalSets))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteSession(session)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun updateSession(session: WorkoutSession, newWeight: Double, newReps: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Update all sets in the session
            session.sets.forEach { set ->
                database.completedSetDao().update(
                    set.copy(
                        weight = newWeight,
                        completedReps = newReps,
                        plannedReps = newReps
                    )
                )
            }
            
            withContext(Dispatchers.Main) {
                loadWorkoutHistory() // Refresh UI
                Toast.makeText(this@WorkoutHistoryActivity, 
                    R.string.workout_updated, 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun deleteSession(session: WorkoutSession) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Delete all sets in the session
            session.sets.forEach { set ->
                database.completedSetDao().delete(set)
            }
            
            withContext(Dispatchers.Main) {
                loadWorkoutHistory() // Refresh UI
                Toast.makeText(this@WorkoutHistoryActivity, 
                    R.string.workout_deleted, 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}