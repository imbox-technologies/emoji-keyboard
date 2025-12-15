package com.davidperi.emojikeyboard.provider

import android.content.Context
import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import kotlin.collections.plus

interface EmojiProvider {
    suspend fun getCategories(context: Context): List<Category>
}
