package com.davidperi.emojikeyboard

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboard.ui.state.PopupStateMachine
import com.davidperi.emojikeyboard.ui.view.EmojiKeyboardViewV2

class EmojiPopup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val keyboardView = EmojiKeyboardViewV2(context)
    private var stateMachine: PopupStateMachine? = null


    init {
        addView(keyboardView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        this.isVisible = false
    }


    // PUBLIC (external) API
    fun setupWith(editText: EditText) {
        keyboardView.setupWith(editText)
        stateMachine = PopupStateMachine(this, editText)
    }

    fun setConfig(config: EmojiKeyboardConfig) {
        keyboardView.setConfig(config)
    }

    fun toggle() = stateMachine?.toggle()

    fun hide() = stateMachine?.hide()

    fun getState() = stateMachine?.state

    fun setOnStateChangedListener(callback: (PopupState) -> Unit) {
        stateMachine?.onStateChanged = callback
    }


    // Public (internal) API
    internal fun notifyStateChanged(state: PopupState) {
        keyboardView.onStateChanged(state)
    }

    internal fun getSearchContentHeight(): Int {
        return keyboardView.getSearchContentHeight()
    }

    internal fun setInternalHeight(height: Int) {
        keyboardView.setInternalContentHeight(height)
    }

    internal fun updatePopupLayoutHeight(newHeight: Int) {
        this.updateLayoutParams { height = newHeight }
    }

    internal fun isSearchFocused(): Boolean {
        return keyboardView.isSearchFocused()
    }

}