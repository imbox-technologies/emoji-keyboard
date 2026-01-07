package com.davidperi.emojikeyboard.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

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

    fun bind(item: EmojiListItem.Spacer) {
        itemView.updateLayoutParams {
            if (!item.isFiller) {
                width = 40.dp
                height = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                width = 1
                height = 1
            }
        }
    }
}