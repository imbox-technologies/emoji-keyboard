package com.davidperi.emojikeyboard.ui.adapter

import com.davidperi.emojikeyboard.model.Category

object EmojiListMapper {

    fun map(categories: List<Category>): List<EmojiListItem> {
        return categories.flatMap { category ->
            buildList {
                add(EmojiListItem.Header(category))
                addAll(category.emojis.map { EmojiListItem.EmojiKey(it) })
            }
        }
    }

}
