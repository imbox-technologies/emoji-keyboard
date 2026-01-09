package com.davidperi.emojikeyboard.ui.view

interface EmojiDelegate {
    fun onCategorySelected(index: Int, progress: Float)  // Category Bar
    fun onEmojiClicked(unicode: String)  // Emoji Grid + Search Results
    fun onGridScrolled(position: Int)  // Emoji Grid
    fun onQueryChanged(query: String)  // Search Bar
    fun onSearchFocusChange(hasFocus: Boolean)  // Search Bar
    fun onBackspacePressed()  // Backspace
}