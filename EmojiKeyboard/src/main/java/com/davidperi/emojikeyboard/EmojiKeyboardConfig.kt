package com.davidperi.emojikeyboard

import android.graphics.Typeface
import com.davidperi.emojikeyboard.data.provider.AssetEmojiProvider
import com.davidperi.emojikeyboard.data.provider.EmojiProvider

enum class EmojiLayoutMode {
    ROBOT,
    COOPER
}

data class EmojiKeyboardConfig(
    val provider: EmojiProvider = AssetEmojiProvider,
    val font: Typeface? = null,
    val layoutMode: EmojiLayoutMode = EmojiLayoutMode.COOPER,
)
