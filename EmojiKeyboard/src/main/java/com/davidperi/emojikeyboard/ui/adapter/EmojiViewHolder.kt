package com.davidperi.emojikeyboard.ui.adapter

import android.graphics.Typeface
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.databinding.ItemEmojiBinding
import com.davidperi.emojikeyboard.model.Emoji

class EmojiViewHolder(itemView: View, typeface: Typeface, onClick: (Emoji) -> Unit) : RecyclerView.ViewHolder(itemView) {

    private val binding = ItemEmojiBinding.bind(itemView)
    private var currentEmoji: Emoji? = null

    init {
        // binding.tvEmoji.typeface = typeface
        binding.tvEmoji.setOnClickListener {
            currentEmoji?.let { onClick(it) }
        }
    }

    fun bind(item: EmojiListItem.EmojiKey) {
        currentEmoji = item.emoji
        binding.tvEmoji.text = item.emoji.unicode
        binding.tvEmoji.contentDescription = item.emoji.description
    }
}
