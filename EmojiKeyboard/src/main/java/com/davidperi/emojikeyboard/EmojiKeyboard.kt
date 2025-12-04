package com.davidperi.emojikeyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.davidperi.emojikeyboard.databinding.EmojiKeyboardPopupBinding

class EmojiKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding =
        EmojiKeyboardPopupBinding.inflate(LayoutInflater.from(context), this, true)

    val searchBar: EditText
        private set
        get() = binding.searchBar.searchBar

    val topBar: LinearLayout
        private set
        get() = binding.topBar
}