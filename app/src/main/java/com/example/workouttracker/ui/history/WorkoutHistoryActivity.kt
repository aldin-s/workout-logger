package com.example.workouttracker.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workouttracker.R
import com.example.workouttracker.data.database.WorkoutDatabase
import kotlinx.coroutines.launch

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
        adapter = WorkoutHistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadWorkoutHistory() {
        lifecycleScope.launch {
            val completedSets = database.completedSetDao().getAllSets()
            adapter.submitList(completedSets)
        }
    }
}