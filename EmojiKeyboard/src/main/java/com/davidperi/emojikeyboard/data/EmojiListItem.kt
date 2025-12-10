package com.davidperi.emojikeyboard.data

sealed class EmojiListItem {
    data class Header(val title: String): EmojiListItem()
    data class Emoji(val unicode: String): EmojiListItem()
}
