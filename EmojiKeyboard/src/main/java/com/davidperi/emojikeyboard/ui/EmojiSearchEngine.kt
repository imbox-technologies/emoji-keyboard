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

        val resultsWithScore = allEmojis.mapNotNull { emoji ->
            val score = calculateScore(emoji, normalizedQuery)
            if (score != -1) emoji to score else null
        }

        resultsWithScore.sortedBy { it.second }.map { it.first }
    }

    private fun calculateScore(emoji: Emoji, query: String): Int {
        if (emoji.keywords.any { it.equals(query, ignoreCase = true) }) {
            return 1
        }

        if (emoji.keywords.any { it.startsWith(query, ignoreCase = true) }) {
            return 2
        }

        val normalizedDesc = emoji.description.lowercase().removeAccents()
        val words = normalizedDesc.split(" ", "-")
        if (words.any { it.startsWith(query) }) {
            return 3
        }

        return -1
    }
}