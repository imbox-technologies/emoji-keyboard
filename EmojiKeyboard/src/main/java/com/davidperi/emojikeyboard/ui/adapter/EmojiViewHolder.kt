package com.davidperi.emojikeyboard.ui.adapter

import android.graphics.Typeface
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.model.Emoji
import com.davidperi.emojikeyboard.ui.EmojiCellView

class EmojiViewHolder(
    itemView: View,
    private val typeface: Typeface,
    private val onClick: (Emoji) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val emojiCell = itemView as EmojiCellView
    private var currentEmoji: Emoji? = null

    init {
        emojiCell.typeface = typeface
        emojiCell.setOnClickListener { currentEmoji?.let { onClick(it) } }
    }

    fun bind(item: EmojiListItem.EmojiKey) {
        currentEmoji = item.emoji
        emojiCell.text = item.emoji.unicode
        emojiCell.contentDescription = item.emoji.description
    }
}
