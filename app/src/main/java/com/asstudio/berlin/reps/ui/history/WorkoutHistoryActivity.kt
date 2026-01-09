package com.asstudio.berlin.reps.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asstudio.berlin.reps.R
import com.asstudio.berlin.reps.data.database.WorkoutDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class WorkoutHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: View
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
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
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
            try {
                val completedSets = database.completedSetDao().getAllSets()
                val groupedItems = HistoryGrouper.groupByDate(completedSets, this@WorkoutHistoryActivity)
                
                withContext(Dispatchers.Main) {
                    if (groupedItems.isEmpty()) {
                        showEmptyState()
                    } else {
                        showHistoryList()
                        adapter.submitList(groupedItems)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("WorkoutHistory", "Error loading history", e)
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@WorkoutHistoryActivity)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.error_load_history)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }
            }
        }
    }
    
    private fun showEmptyState() {
        emptyStateLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    
    private fun showHistoryList() {
        emptyStateLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
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
        val bottomSheet = EditWorkoutBottomSheet(session) { weight, reps, sets ->
            updateSession(session, weight, reps, sets)
        }
        bottomSheet.show(supportFragmentManager, EditWorkoutBottomSheet.TAG)
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
    
    private fun updateSession(session: WorkoutSession, newWeight: Double, newReps: Int, newSets: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentSetCount = session.sets.size
                
                when {
                    newSets == currentSetCount -> {
                        // Same number of sets: just update existing ones
                        session.sets.forEach { set ->
                            database.completedSetDao().update(
                                set.copy(
                                    weight = newWeight,
                                    completedReps = newReps,
                                    plannedReps = newReps
                                )
                            )
                        }
                    }
                    newSets < currentSetCount -> {
                        // Fewer sets: update first N, delete rest
                        session.sets.take(newSets).forEach { set ->
                            database.completedSetDao().update(
                                set.copy(
                                    weight = newWeight,
                                    completedReps = newReps,
                                    plannedReps = newReps
                                )
                            )
                        }
                        session.sets.drop(newSets).forEach { set ->
                            database.completedSetDao().delete(set)
                        }
                    }
                    else -> {
                        // More sets: update existing, create new ones
                        session.sets.forEach { set ->
                            database.completedSetDao().update(
                                set.copy(
                                    weight = newWeight,
                                    completedReps = newReps,
                                    plannedReps = newReps
                                )
                            )
                        }
                        // Create additional sets
                        val templateSet = session.sets.first()
                        repeat(newSets - currentSetCount) { index ->
                            database.completedSetDao().insert(
                                templateSet.copy(
                                    id = 0, // Auto-generate new ID
                                    setNumber = currentSetCount + index + 1,
                                    weight = newWeight,
                                    completedReps = newReps,
                                    plannedReps = newReps
                                )
                            )
                        }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    loadWorkoutHistory() // Refresh UI
                    Toast.makeText(this@WorkoutHistoryActivity, 
                        R.string.workout_updated, 
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("WorkoutHistory", "Error updating workout", e)
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@WorkoutHistoryActivity)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.error_update_workout)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }
            }
        }
    }
    
    private fun deleteSession(session: WorkoutSession) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
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
            } catch (e: Exception) {
                android.util.Log.e("WorkoutHistory", "Error deleting workout", e)
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@WorkoutHistoryActivity)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.error_delete_workout)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                }
            }
        }
    }
}