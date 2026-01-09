package com.asstudio.berlin.reps.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.asstudio.berlin.reps.R
import com.asstudio.berlin.reps.data.model.CustomExercise
import com.google.android.material.card.MaterialCardView
import java.util.Collections

class DraggableExerciseAdapter(
    private val onExerciseClick: (CustomExercise) -> Unit,
    private val onDeleteClick: (CustomExercise) -> Unit,
    private val onItemMoved: (List<CustomExercise>) -> Unit
) : ListAdapter<CustomExercise, DraggableExerciseAdapter.ViewHolder>(DiffCallback()) {

    private var exercises = mutableListOf<CustomExercise>()
    private var isDragging = false

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).name.hashCode().toLong()
    }

    override fun submitList(list: List<CustomExercise>?) {
        // Don't update during drag operation
        if (isDragging) return
        
        exercises = list?.toMutableList() ?: mutableListOf()
        // Use submitList(list, null) to disable animation and instant update
        super.submitList(exercises.toList(), null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        isDragging = true
        
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(exercises, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(exercises, i, i - 1)
            }
        }
        
        // Update sortOrder based on new positions
        exercises.forEachIndexed { index, exercise ->
            exercises[index] = exercise.copy(sortOrder = index + 1)
        }
        
        notifyItemMoved(fromPosition, toPosition)
        return true
    }
    
    fun onDragFinished() {
        isDragging = false
        // Submit final sorted list and trigger save (no animation)
        super.submitList(exercises.toList(), null)
        onItemMoved(exercises.toList())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.exerciseCard)
        private val nameText: TextView = itemView.findViewById(R.id.exerciseNameText)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(exercise: CustomExercise) {
            nameText.text = exercise.name
            deleteButton.visibility = View.VISIBLE
            
            card.setOnClickListener {
                onExerciseClick(exercise)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(exercise)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CustomExercise>() {
        override fun areItemsTheSame(oldItem: CustomExercise, newItem: CustomExercise): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: CustomExercise, newItem: CustomExercise): Boolean {
            return oldItem == newItem
        }
    }
}
