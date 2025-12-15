package com.davidperi.emojikeyboard.provider

import com.davidperi.emojikeyboard.model.Emoji
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
