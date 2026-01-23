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

import com.imbox.emojikeyboard.data.model.Emoji
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryJson(
    val id: String,
    val name: String,
    val emojis: List<EmojiJson>
)

@Serializable
data class EmojiJson(
    @SerialName("u") val unicode: String,
    @SerialName("d") val description: String,
    @SerialName("k") val keywords: List<String> = emptyList(),
    @SerialName("v") val variants: List<EmojiJson> = emptyList()
) {
    fun toDomain(): Emoji {
        return Emoji(
            unicode = unicode,
            description = description,
            keywords = keywords,
            variants = variants.map { it.toDomain() }
        )
    }
}
