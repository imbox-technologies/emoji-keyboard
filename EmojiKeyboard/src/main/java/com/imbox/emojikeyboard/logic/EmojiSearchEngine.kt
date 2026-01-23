/*
 * Copyright 2026 - David Periañez and contributors
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

package com.imbox.emojikeyboard.logic

import com.imbox.emojikeyboard.data.model.Category
import com.imbox.emojikeyboard.data.model.Emoji
import com.imbox.emojikeyboard.utils.DisplayUtils.removeAccents
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
        val queryWords = normalizedQuery.split(" ").filter { it.isNotBlank() }

        if (queryWords.isEmpty()) return@withContext emptyList()

        val resultsWithScore = allEmojis.mapNotNull { emoji ->
            val score = calculateScore(emoji, queryWords, normalizedQuery)
            if (score != -1) emoji to score else null
        }

        resultsWithScore.sortedBy { it.second }.map { it.first }
    }

    private fun calculateScore(emoji: Emoji, queryWords: List<String>, fullQuery: String): Int {
        val normalizedKeywords = emoji.keywords.map { it.lowercase().removeAccents() }
        val normalizedDesc = emoji.description.lowercase().removeAccents()
        val descWords = normalizedDesc.split(" ", "-").filter { it.isNotBlank() }

        if (queryWords.size == 1) {
            val singleWord = queryWords.first()

            if (normalizedKeywords.any { it.equals(singleWord, ignoreCase = true) }) {
                return 1
            }

            if (normalizedKeywords.any { it.startsWith(singleWord, ignoreCase = true) }) {
                return 2
            }

            if (descWords.any { it.startsWith(singleWord) }) {
                return 3
            }

            if (normalizedDesc.contains(singleWord)) {
                return 4
            }

            return -1
        }

        val matchedWords = mutableSetOf<String>()
        var exactKeywordMatches = 0
        var startsWithKeywordMatches = 0
        var descWordMatches = 0
        var descContainsMatches = 0

        for (word in queryWords) {
            var wordMatched = false

            if (normalizedKeywords.any { it.equals(word, ignoreCase = true) }) {
                exactKeywordMatches++
                wordMatched = true
            } else if (normalizedKeywords.any { it.startsWith(word, ignoreCase = true) }) {
                startsWithKeywordMatches++
                wordMatched = true
            } else if (descWords.any { it.startsWith(word) }) {
                descWordMatches++
                wordMatched = true
            } else if (normalizedDesc.contains(word)) {
                descContainsMatches++
                wordMatched = true
            }

            if (wordMatched) {
                matchedWords.add(word)
            }
        }

        if (matchedWords.size < queryWords.size) {
            return -1
        }

        val allMatched = matchedWords.size == queryWords.size

        if (allMatched && exactKeywordMatches == queryWords.size) {
            return 1
        }

        if (allMatched && (exactKeywordMatches + startsWithKeywordMatches) == queryWords.size) {
            return 2
        }

        if (allMatched && (exactKeywordMatches + startsWithKeywordMatches + descWordMatches) > 0) {
            return 3
        }

        if (allMatched) {
            return 4
        }

        return -1
    }
}