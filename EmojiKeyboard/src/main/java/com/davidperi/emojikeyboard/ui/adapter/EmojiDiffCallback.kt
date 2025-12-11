package com.davidperi.emojikeyboard.ui.adapter

import androidx.recyclerview.widget.DiffUtil

object EmojiDiffCallback : DiffUtil.ItemCallback<EmojiListItem>() {
    override fun areItemsTheSame(oldItem: EmojiListItem, newItem: EmojiListItem): Boolean {
        return when {
            oldItem is EmojiListItem.Header && newItem is EmojiListItem.Header -> oldItem.category.id == newItem.category.id
            oldItem is EmojiListItem.EmojiKey && newItem is EmojiListItem.EmojiKey -> oldItem.emoji.unicode == newItem.emoji.unicode
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: EmojiListItem, newItem: EmojiListItem): Boolean {
        return oldItem == newItem
    }
}
