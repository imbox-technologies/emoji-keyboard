/*
 * Copyright 2026 - IMBox Technologies and contributors
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

package com.imbox.emojikeyboard.data.provider

import android.content.Context
import com.imbox.emojikeyboard.data.model.Category
import io.github.davidimbox.emojikeyboard.R
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
        "faces" to R.drawable.emjkb_smile,
        "nature" to R.drawable.emjkb_dog,
        "food" to R.drawable.emjkb_apple,
        "activities" to R.drawable.emjkb_volleyball,
        "travel" to R.drawable.emjkb_car,
        "objects" to R.drawable.emjkb_lightbulb,
        "symbols" to R.drawable.emjkb_heart,
        "flags" to R.drawable.emjkb_flag
    )

    private var cachedCategories: List<Category>? = null

    private suspend fun loadCategories(context: Context): List<Category> {
        return cachedCategories ?: withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("providers/emojis_en.json")
                    .bufferedReader()
                    .use { it.readText() }

                val dtos = jsonParser.decodeFromString<List<CategoryJson>>(jsonString)

                val domainCategories = dtos.map { dto ->
                    Category(
                        id = dto.id,
                        name = dto.name,
                        icon = categoryIcons[dto.id] ?: R.drawable.emjkb_smile,
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
