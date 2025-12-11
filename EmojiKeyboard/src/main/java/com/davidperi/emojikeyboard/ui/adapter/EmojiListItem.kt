package com.davidperi.emojikeyboard.ui.adapter

import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.model.Emoji

sealed class EmojiListItem {
    data class Header(val category: Category): EmojiListItem()
    data class EmojiKey(val emoji: Emoji): EmojiListItem()
}