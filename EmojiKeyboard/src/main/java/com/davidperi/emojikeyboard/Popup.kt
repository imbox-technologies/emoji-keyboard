package com.davidperi.emojikeyboard

import android.widget.EditText
import com.davidperi.emojikeyboard.ui.state.PopupState

internal interface InternalPopup {
    fun showKeyboard()
    fun hideKeyboard()

    fun updatePopupHeight(height: Int)
    fun popupStateChanged(state: PopupState)

    fun getSearchContentHeight(): Int
    fun getKeyboardStandardHeight(): Int
}

interface PopupApi {
    val state: PopupState

    fun bindTo(editText: EditText)
    fun setConfig(config: EmojiKeyboardConfig)

    fun toggle()
    fun hide()

    fun setOnPopupStateChangedListener(callback: (PopupState) -> Unit)
}