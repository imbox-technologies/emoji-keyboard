package com.davidperi.emojikeyboard.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import com.davidperi.emojikeyboard.utils.PrefsManager.Companion.PREFS_KEY
import java.util.LinkedList

class RecentEmojiManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    private var recentUnicodes: LinkedList<String> = LinkedList()
    private val handler = Handler(Looper.getMainLooper())
    private var persistRunnable: Runnable? = null

    companion object {
        private const val PREFS_RECENTS_KEY = "emoji_keyboard_recent_emojis_unicode"
        private const val MAX_RECENTS = 36
        private const val PERSIST_DELAY_MS = 1000L
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
        schedulePersist()
    }

    private fun schedulePersist() {
        persistRunnable?.let { handler.removeCallbacks(it) }
        persistRunnable = Runnable {
            persist()
            persistRunnable = null
        }
        handler.postDelayed(persistRunnable!!, PERSIST_DELAY_MS)
    }

    fun forcePersist() {
        persistRunnable?.let { handler.removeCallbacks(it) }
        persistRunnable = null
        persist()
    }

    private fun persist() {
        val stringToSave = recentUnicodes.joinToString(",")
        prefs.edit { putString(PREFS_RECENTS_KEY, stringToSave) }
    }
}