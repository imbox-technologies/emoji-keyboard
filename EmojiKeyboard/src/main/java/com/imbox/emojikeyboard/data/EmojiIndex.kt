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

package com.imbox.emojikeyboard.data

import com.imbox.emojikeyboard.data.model.Category
import com.imbox.emojikeyboard.data.model.Emoji

class EmojiIndex private constructor(
    private val supportedEmojis: Set<String>
) {

    fun contains(unicode: String): Boolean {
        return supportedEmojis.contains(unicode)
    }

    val size: Int
        get() = supportedEmojis.size

    companion object {
        fun build(categories: List<Category>): EmojiIndex {
            val emojis = HashSet<String>(estimateCapacity(categories))

            for (category in categories) {
                for (emoji in category.emojis) {
                    addEmojiWithVariants(emojis, emoji)
                }
            }

            return EmojiIndex(emojis)
        }

        private fun addEmojiWithVariants(set: MutableSet<String>, emoji: Emoji) {
            set.add(emoji.unicode)
            for (variant in emoji.variants) {
                addEmojiWithVariants(set, variant)
            }
        }

        private fun estimateCapacity(categories: List<Category>): Int {
            var count = 0
            for (category in categories) {
                for (emoji in category.emojis) {
                    count += 1 + emoji.variants.size
                }
            }
            return count
        }
    }
}
