package com.davidperi.emojikeyboard.ui.adapter

import android.graphics.Typeface
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.model.Emoji
import com.davidperi.emojikeyboard.utils.BindingUtils.inflate
import com.davidperi.emojikeyboard.utils.EmojiFontManager

class EmojiAdapter(
    private val onEmojiClicked: (Emoji) -> Unit
) : ListAdapter<EmojiListItem, RecyclerView.ViewHolder>(EmojiDiffCallback) {

    private lateinit var cachedTypeface: Typeface

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_EMOJI = 1
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        cachedTypeface = EmojiFontManager.getTypeface(recyclerView.context)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EmojiListItem.Header -> VIEW_TYPE_HEADER
            is EmojiListItem.EmojiKey -> VIEW_TYPE_EMOJI
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
            is EmojiViewHolder -> holder.bind(item as EmojiListItem.EmojiKey, cachedTypeface, onEmojiClicked)
        }
    }
}
