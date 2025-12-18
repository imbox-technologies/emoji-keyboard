package com.davidperi.emojikeyboard.provider

import android.content.Context
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

object AssetEmojiProvider : EmojiProvider {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val categoryIcons = mapOf(
        "faces" to R.drawable.smile,
        "nature" to R.drawable.dog,
        "food" to R.drawable.apple,
        "activities" to R.drawable.volleyball,
        "travel" to R.drawable.car_front,
        "objects" to R.drawable.lightbulb,
        "symbols" to R.drawable.heart,
        "flags" to R.drawable.flag
    )

    private var cachedCategories: List<Category>? = null

    private suspend fun loadCategories(context: Context): List<Category> {
        return cachedCategories ?: withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("providers/emojis_en_v2.json")
                    .bufferedReader()
                    .use { it.readText() }

                val dtos = jsonParser.decodeFromString<List<CategoryJson>>(jsonString)

                val domainCategories = dtos.map { dto ->
                    Category(
                        id = dto.id,
                        name = dto.name,
                        icon = categoryIcons[dto.id] ?: R.drawable.smile,
                        emojis = dto.emojis.map { it.toDomain() }
                    )
                }

                cachedCategories = domainCategories
                domainCategories
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    override suspend fun getCategories(context: Context): List<Category> {
        return loadCategories(context)
    }
}
