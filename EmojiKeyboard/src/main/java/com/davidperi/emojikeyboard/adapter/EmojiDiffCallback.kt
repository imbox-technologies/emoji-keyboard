package com.davidperi.emojikeyboard.adapter

import androidx.recyclerview.widget.DiffUtil
import com.davidperi.emojikeyboard.data.EmojiListItem

object EmojiDiffCallback : DiffUtil.ItemCallback<EmojiListItem>() {
    override fun areItemsTheSame(oldItem: EmojiListItem, newItem: EmojiListItem): Boolean {
        return when {
            oldItem is EmojiListItem.Header && newItem is EmojiListItem.Header -> oldItem.title == newItem.title
            oldItem is EmojiListItem.Emoji && newItem is EmojiListItem.Emoji -> oldItem.unicode == newItem.unicode
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: EmojiListItem, newItem: EmojiListItem): Boolean {
        return oldItem == newItem
    }
}
