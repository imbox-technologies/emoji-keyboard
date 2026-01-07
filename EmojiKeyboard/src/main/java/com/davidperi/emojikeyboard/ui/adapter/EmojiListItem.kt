package com.davidperi.emojikeyboard.ui.adapter

import com.davidperi.emojikeyboard.data.model.Category
import com.davidperi.emojikeyboard.data.model.Emoji

sealed class EmojiListItem {
    data class Header(val category: Category): EmojiListItem()
    data class EmojiKey(val emoji: Emoji): EmojiListItem()
    data class Spacer(val isFiller: Boolean) : EmojiListItem()
}