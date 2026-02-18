package com.imbox.emojikeyboardtest.prefs

import android.content.Context
import android.content.SharedPreferences
import com.imbox.emojikeyboard.EmojiLayoutMode
import com.imbox.emojikeyboard.EmojiThemeMode

class KeyboardConfigPrefs(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var fontId: String
        get() = prefs.getString(KEY_FONT_ID, DEFAULT_FONT_ID) ?: DEFAULT_FONT_ID
        set(value) = prefs.edit().putString(KEY_FONT_ID, value).apply()

    var layoutMode: EmojiLayoutMode
        get() = runCatching {
            EmojiLayoutMode.valueOf(prefs.getString(KEY_LAYOUT_MODE, EmojiLayoutMode.COOPER.name) ?: EmojiLayoutMode.COOPER.name)
        }.getOrDefault(EmojiLayoutMode.COOPER)
        set(value) = prefs.edit().putString(KEY_LAYOUT_MODE, value.name).apply()

    var themeMode: EmojiThemeMode
        get() = runCatching {
            EmojiThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, EmojiThemeMode.DEFAULT.name) ?: EmojiThemeMode.DEFAULT.name)
        }.getOrDefault(EmojiThemeMode.DEFAULT)
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()

    fun save(fontId: String, layoutMode: EmojiLayoutMode, themeMode: EmojiThemeMode) {
        prefs.edit()
            .putString(KEY_FONT_ID, fontId)
            .putString(KEY_LAYOUT_MODE, layoutMode.name)
            .putString(KEY_THEME_MODE, themeMode.name)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "keyboard_config"
        private const val KEY_FONT_ID = "font_id"
        private const val KEY_LAYOUT_MODE = "layout_mode"
        private const val KEY_THEME_MODE = "theme_mode"
        const val DEFAULT_FONT_ID = "cooper"
    }
}
