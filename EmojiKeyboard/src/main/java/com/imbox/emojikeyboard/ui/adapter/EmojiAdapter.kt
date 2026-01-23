/*
 * Copyright 2026 - David Periañez and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.imbox.emojikeyboard.ui.adapter

import android.graphics.Typeface
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.imbox.emojikeyboard.EmojiManager
import com.imbox.emojikeyboard.data.model.Emoji
import com.imbox.emojikeyboard.ui.view.components.EmojiCellView
import com.imbox.emojikeyboard.ui.view.components.EmojiHeaderItem

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
