package com.davidperi.emojikeyboard.data.model

import androidx.annotation.DrawableRes

data class Category (
    val id: String,
    val name: String,
    @DrawableRes val icon: Int,
    val emojis: List<Emoji>
)