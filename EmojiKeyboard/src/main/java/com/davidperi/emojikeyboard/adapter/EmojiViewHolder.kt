package com.davidperi.emojikeyboard.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.data.EmojiListItem
import com.davidperi.emojikeyboard.databinding.ItemEmojiBinding

class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemEmojiBinding.bind(itemView)

    fun bind(item: EmojiListItem.Emoji) {
        binding.tvEmoji.text = item.unicode
    }
}
