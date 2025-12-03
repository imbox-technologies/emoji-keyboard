package com.davidperi.emojikeyboard

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class EmojiKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.emoji_keyboard_popup, this)
    }
}