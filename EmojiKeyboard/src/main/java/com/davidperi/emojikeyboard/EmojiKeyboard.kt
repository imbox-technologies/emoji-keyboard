package com.davidperi.emojikeyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.davidperi.emojikeyboard.databinding.EmojiKeyboardPopupBinding

class EmojiKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.emoji_keyboard_popup, this)
    }
}