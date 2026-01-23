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

import io.github.davidimbox.emojikeyboard.R
import com.imbox.emojikeyboard.data.model.Category
import com.imbox.emojikeyboard.data.model.Emoji

object EmojiListMapper {
    // Transform from List<Category> to List<EmojiListItem>
    // Also return category ranges for scrolling logic
    // It depends on the Config (layoutMode)

    data class Result(
        val items: List<EmojiListItem>,
        val categoryRanges: List<IntRange>
    )

    fun map(
        categories: List<Category>,
        isVertical: Boolean,
        spanCount: Int,
        includeHeader: Boolean = true
    ): Result {
        val list = mutableListOf<EmojiListItem>()
        val ranges = mutableListOf<IntRange>()

        categories.forEach { category ->
            val startIndex = list.size

            // In vertical mode, category names are displayed
            if (isVertical && includeHeader) {
                list.add(EmojiListItem.Header(category))
            }

            category.emojis.forEach {
                list.add(EmojiListItem.EmojiKey(it))
            }

            // In horizontal mode could be necessary to add filler views
            if (!isVertical) {
                val currentItemsCount = category.emojis.size
                val remainder = currentItemsCount % spanCount

                if (remainder != 0) {
                    val spacersNeeded = spanCount - remainder
                    repeat(spacersNeeded) {
                        list.add(EmojiListItem.Spacer(true))
                    }
                }
                repeat(spanCount) {
                    list.add(EmojiListItem.Spacer(false))
                }
            }

            val endIndex = list.size - 1
            ranges.add(startIndex..endIndex)
        }

        return Result(list, ranges)
    }

    fun mapRecents(
        unicodes: List<String>,
        isVertical: Boolean,
        spanCount: Int
    ): Result {
        val emojiItems = unicodes.map { Emoji(it) }

        if (emojiItems.isEmpty()) {
            return Result(emptyList(), emptyList())
        }

        val recentsCategory = Category(
            id = "recents",
            name = "Recents",
            icon = R.drawable.emjkb_clock,
            emojis = emojiItems
        )

        return map(listOf(recentsCategory), isVertical, spanCount, false)
    }

    fun mapSuggestions(unicodes: List<String>): List<EmojiListItem> {
        return unicodes.map { EmojiListItem.EmojiKey(Emoji(it)) }
    }
}