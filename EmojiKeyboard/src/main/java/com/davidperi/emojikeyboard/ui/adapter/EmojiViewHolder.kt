package com.davidperi.emojikeyboard.ui.adapter

import android.graphics.Typeface
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.databinding.ItemEmojiBinding
import com.davidperi.emojikeyboard.model.Emoji

class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemEmojiBinding.bind(itemView)

    fun bind(item: EmojiListItem.EmojiKey, cachedTypeface: Typeface, onClick: (Emoji) -> Unit) {
        binding.tvEmoji.apply {
            text = item.emoji.unicode
            typeface = cachedTypeface
            contentDescription = item.emoji.description
            setOnClickListener { onClick(item.emoji) }
        }
    }
}
