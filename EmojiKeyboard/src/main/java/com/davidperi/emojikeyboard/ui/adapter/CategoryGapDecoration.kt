package com.davidperi.emojikeyboard.ui.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CategoryGapDecoration(
    private val categoryRanges: List<IntRange>,
    private val gapSize: Int,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val range = categoryRanges.find { position in it } ?: return
        if (range == categoryRanges.last()) return

        val categorySize = range.last - range.first + 1
        val totalColumns = categorySize / spanCount
        val relativePos = position - range.first
        val currentColumn = relativePos / spanCount

        if (currentColumn == totalColumns - 1) {
            outRect.right = gapSize
        }
    }
}
