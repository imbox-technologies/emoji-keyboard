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
