package com.davidperi.emojikeyboard.ui.adapter

import android.graphics.Typeface
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.EmojiManager
import com.davidperi.emojikeyboard.data.model.Emoji
import com.davidperi.emojikeyboard.ui.view.components.EmojiCellView
import com.davidperi.emojikeyboard.ui.view.components.EmojiHeaderItem

class EmojiAdapter(
    private val onEmojiClicked: (Emoji) -> Unit
) : ListAdapter<EmojiListItem, RecyclerView.ViewHolder>(EmojiDiffCallback) {

    private lateinit var cachedTypeface: Typeface

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_EMOJI = 1
        const val VIEW_TYPE_SPACER = 2
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        cachedTypeface = EmojiManager.getTypeface()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EmojiListItem.Header -> VIEW_TYPE_HEADER
            is EmojiListItem.EmojiKey -> VIEW_TYPE_EMOJI
            is EmojiListItem.Spacer -> VIEW_TYPE_SPACER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(EmojiHeaderItem(parent.context))
            VIEW_TYPE_EMOJI -> EmojiViewHolder(EmojiCellView(parent.context), cachedTypeface, onEmojiClicked)
            VIEW_TYPE_SPACER -> SpacerViewHolder.create(parent)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as EmojiListItem.Header)
            is EmojiViewHolder -> holder.bind(item as EmojiListItem.EmojiKey)
            is SpacerViewHolder -> holder.bind(item as EmojiListItem.Spacer)
        }
    }
}
