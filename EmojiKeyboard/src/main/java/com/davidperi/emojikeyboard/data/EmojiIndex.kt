package com.davidperi.emojikeyboard.data

import com.davidperi.emojikeyboard.data.model.Category
import com.davidperi.emojikeyboard.data.model.Emoji

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
