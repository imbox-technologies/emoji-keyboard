package com.davidperi.emojikeyboard.utils

import android.content.Context
import android.graphics.Typeface

object EmojiFontManager {
    private var typeface: Typeface? = null
    private const val FONT_PATH = "fonts/Twemoji.ttf"

    fun getTypeface(context: Context): Typeface {
        return typeface ?: synchronized(this) {
            typeface ?: Typeface.createFromAsset(context.assets, FONT_PATH).also {
                typeface = it
            }
        }
    }
}
