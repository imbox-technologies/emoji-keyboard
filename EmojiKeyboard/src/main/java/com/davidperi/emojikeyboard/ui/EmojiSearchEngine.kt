package com.davidperi.emojikeyboard.ui

import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.model.Emoji
import com.davidperi.emojikeyboard.utils.DisplayUtils.removeAccents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmojiSearchEngine {

    private val allEmojis = mutableListOf<Emoji>()

    fun initialize(categories: List<Category>) {
        allEmojis.clear()
        categories.forEach { category ->
            allEmojis.addAll(category.emojis)
        }
    }

    suspend fun search(query: String): List<Emoji> = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext emptyList()
        val normalizedQuery = query.trim().lowercase().removeAccents()
        allEmojis.filter { emoji ->
            if (emoji.description.lowercase().contains(normalizedQuery)) return@filter true

            emoji.keywords.any { keyword ->
                keyword.lowercase().contains(normalizedQuery)
            }
        }
    }
}