package com.davidperi.emojikeyboard.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SpacerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): SpacerViewHolder {
            val view = View(parent.context).apply {
                visibility = View.INVISIBLE
                layoutParams = ViewGroup.LayoutParams(1, 1)
            }
            return SpacerViewHolder(view)
        }
    }
}