/*
 * Copyright 2026 - IMBox Technologies and contributors
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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.imbox.emojikeyboard.data.model.Emoji
import com.imbox.emojikeyboard.ui.view.components.EmojiCellView

class EmojiViewHolder(
    itemView: View,
    private val typeface: Typeface,
    private val onClick: (Emoji) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val emojiCell = itemView as EmojiCellView
    private var currentEmoji: Emoji? = null

    init {
        emojiCell.typeface = typeface
        emojiCell.setOnClickListener { currentEmoji?.let { onClick(it) } }
    }

    fun bind(item: EmojiListItem.EmojiKey) {
        currentEmoji = item.emoji
        emojiCell.text = item.emoji.unicode
        emojiCell.contentDescription = item.emoji.description
    }
}
