package com.davidperi.emojikeyboard.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.data.EmojiListItem
import com.davidperi.emojikeyboard.databinding.ItemHeaderBinding

class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemHeaderBinding.bind(itemView)

    fun bind(item: EmojiListItem.Header) {
        binding.tvCategoryName.text = item.title
    }
}
