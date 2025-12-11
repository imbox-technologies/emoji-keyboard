package com.davidperi.emojikeyboard.model

import androidx.annotation.DrawableRes
import com.davidperi.emojikeyboard.model.Emoji

data class Category (
    val id: String,
    val name: String,
    @DrawableRes val icon: Int,
    val emojis: List<Emoji>
)