package com.asstudio.berlin.reps.ui.workout

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ExerciseItemTouchHelper(
    private val adapter: DraggableExerciseAdapter
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPos = viewHolder.bindingAdapterPosition
        val toPos = target.bindingAdapterPosition
        if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) {
            return false
        }
        return adapter.onItemMove(fromPos, toPos)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // No swipe action
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.alpha = 0.7f
            viewHolder?.itemView?.scaleX = 1.05f
            viewHolder?.itemView?.scaleY = 1.05f
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        
        viewHolder.itemView.alpha = 1.0f
        viewHolder.itemView.scaleX = 1.0f
        viewHolder.itemView.scaleY = 1.0f
        
        // Notify adapter that drag is finished
        adapter.onDragFinished()
    }
}
