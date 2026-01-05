package com.davidperi.emojikeyboard.data.provider

import android.content.Context
import com.davidperi.emojikeyboard.data.model.Category

interface EmojiProvider {
    suspend fun getCategories(context: Context): List<Category>
}
