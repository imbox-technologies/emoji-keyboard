package com.davidperi.emojikeyboard.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.ui.view.components.EmojiHeaderItem

class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val headerItem = itemView as EmojiHeaderItem

    fun bind(item: EmojiListItem.Header) {
        headerItem.text = item.category.name
    }
}
