package com.davidperi.emojikeyboard.model

data class Emoji (
    val unicode: String,
    val description: String = "",
    val keywords: List<String> = emptyList(),
    val variants: List<Emoji> = emptyList()
) {
    val hasVariants: Boolean
        get() = variants.isNotEmpty()
}
