package com.davidperi.emojikeyboard.utils

import android.content.Context
import android.graphics.Typeface

object EmojiFontManager {
    private var typeface: Typeface? = null
    private const val DEFAULT_FONT_PATH = "fonts/TwemojiV2.ttf"

    fun setCustomTypeface(typeface: Typeface) {
        this.typeface = typeface
    }

    fun getTypeface(context: Context): Typeface {
        return typeface ?: synchronized(this) {
            typeface ?: Typeface.createFromAsset(context.assets, DEFAULT_FONT_PATH).also {
                typeface = it
            }
        }
    }
}
