package com.davidperi.emojikeyboard.ui.model

import android.graphics.Typeface
import com.davidperi.emojikeyboard.provider.DefaultEmojiProvider
import com.davidperi.emojikeyboard.provider.EmojiProvider

enum class EmojiLayoutMode {
    ROBOT,
    COOPER
}

data class EmojiKeyboardConfig(
    val provider: EmojiProvider = DefaultEmojiProvider,
    val font: Typeface? = null,
    val layoutMode: EmojiLayoutMode = EmojiLayoutMode.ROBOT,
)
