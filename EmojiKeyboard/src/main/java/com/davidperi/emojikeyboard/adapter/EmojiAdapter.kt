package com.davidperi.emojikeyboard.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.data.EmojiListItem
import com.davidperi.emojikeyboard.utils.BindingUtils.inflate

class EmojiAdapter(
    private val onEmojiClicked: (String) -> Unit
) : ListAdapter<EmojiListItem, RecyclerView.ViewHolder>(EmojiDiffCallback) {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_EMOJI = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EmojiListItem.Header -> VIEW_TYPE_HEADER
            is EmojiListItem.Emoji -> VIEW_TYPE_EMOJI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(parent.inflate(R.layout.item_header))
            VIEW_TYPE_EMOJI -> EmojiViewHolder(parent.inflate(R.layout.item_emoji))
            else -> EmojiViewHolder(parent.inflate(R.layout.item_header))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as EmojiListItem.Header)
            is EmojiViewHolder -> holder.bind(item as EmojiListItem.Emoji)
        }
    }
}
