package com.davidperi.emojikeyboard.ui.adapter

import com.davidperi.emojikeyboard.model.Category

object EmojiListMapper {

    fun map(categories: List<Category>, includeHeaders: Boolean): List<EmojiListItem> {
        return categories.flatMap { category ->
            buildList {
                if (includeHeaders) add(EmojiListItem.Header(category))
                addAll(category.emojis.map { EmojiListItem.EmojiKey(it) })
            }
        }
    }

}
