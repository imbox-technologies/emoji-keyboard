package com.davidperi.emojikeyboard.ui

import android.content.Context
import androidx.core.content.edit
import java.util.LinkedList

class RecentEmojiManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    private var recentUnicodes: LinkedList<String> = LinkedList()

    companion object {
        private const val PREFS_KEY = "emoji_keyboard_prefs"
        private const val PREFS_RECENTS_KEY = "emoji_keyboard_recent_emojis_unicode"
        private const val MAX_RECENTS = 36
    }

    init {
        loadRecents()
    }

    private fun loadRecents() {
        val savedString = prefs.getString(PREFS_RECENTS_KEY, "") ?: ""
        if (savedString.isNotEmpty()) {
            recentUnicodes.clear()
            recentUnicodes.addAll(savedString.split(","))
        }
    }

    fun getRecentUnicodes(): List<String> {
        return recentUnicodes.toList()
    }

    fun addEmoji(unicode: String) {
        recentUnicodes.remove(unicode)
        recentUnicodes.addFirst(unicode)
        if (recentUnicodes.size > MAX_RECENTS) {
            recentUnicodes.removeLast()
        }
        persist()
    }

    private fun persist() {
        val stringToSave = recentUnicodes.joinToString(",")
        prefs.edit { putString(PREFS_RECENTS_KEY, stringToSave) }
    }
}