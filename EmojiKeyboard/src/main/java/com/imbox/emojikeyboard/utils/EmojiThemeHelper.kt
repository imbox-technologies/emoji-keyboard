package com.imbox.emojikeyboard.utils

import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import com.imbox.emojikeyboard.EmojiThemeMode
import com.imbox.emojikeyboard.R

internal object EmojiThemeHelper {

    fun wrapContext(context: Context, themeMode: EmojiThemeMode): Context {
        val themeRes = when (themeMode) {
            EmojiThemeMode.DEFAULT -> R.style.EmojiKeyboardTheme
            EmojiThemeMode.LIGHT -> R.style.EmojiKeyboardTheme_Light
            EmojiThemeMode.DARK -> R.style.EmojiKeyboardTheme_Dark
        }
        return ContextThemeWrapper(context, themeRes)
    }

    fun resolveColor(context: Context, @AttrRes attr: Int, fallback: Int = Color.MAGENTA): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, fallback)
        typedArray.recycle()
        return color
    }
}
