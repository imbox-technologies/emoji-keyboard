package com.davidperi.emojikeyboard.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    var lastKeyboardHeight: Int
        get() = prefs.getInt(KEY_HEIGHT, DEFAULT_HEIGHT_DP.dp)
        set(value) = prefs.edit { putInt(KEY_HEIGHT, value) }

    companion object {
        const val PREFS_KEY = "emoji_keyboard_prefs"
        const val DEFAULT_HEIGHT_DP = 300
        private const val KEY_HEIGHT = "emoji_keyboard_prefs_height"
    }
}