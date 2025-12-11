package com.davidperi.emojikeyboard.provider

import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import kotlin.collections.plus

interface EmojiProvider {
    fun getCategories(): List<Category>
}
