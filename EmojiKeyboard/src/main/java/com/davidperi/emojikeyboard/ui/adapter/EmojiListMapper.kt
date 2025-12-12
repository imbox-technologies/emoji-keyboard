package com.davidperi.emojikeyboard.ui.adapter

import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.ui.model.EmojiLayoutMode

object EmojiListMapper {

    data class Result(
        val items: List<EmojiListItem>,
        val categoryRanges: List<IntRange>
    )

    fun map(
        categories: List<Category>,
        mode: EmojiLayoutMode,
        spanCount: Int
    ): Result {

        val list = mutableListOf<EmojiListItem>()
        val ranges = mutableListOf<IntRange>()
        val isHorizontal = mode == EmojiLayoutMode.COOPER

        categories.forEach { category ->
            val startIndex = list.size

            if (!isHorizontal) {
                list.add(EmojiListItem.Header(category))
            }

            category.emojis.forEach {
                list.add(EmojiListItem.EmojiKey(it))
            }

            if (isHorizontal) {
                val currentItemsCount = category.emojis.size
                val remainder = currentItemsCount % spanCount

                if (remainder != 0) {
                    val spacersNeeded = spanCount - remainder
                    repeat(spacersNeeded) {
                        list.add(EmojiListItem.Spacer)
                    }
                }
            }

            val endIndex = list.size - 1
            ranges.add(startIndex..endIndex)
        }

        return Result(list, ranges)
    }

}
