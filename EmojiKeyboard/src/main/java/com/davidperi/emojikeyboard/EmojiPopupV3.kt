package com.davidperi.emojikeyboard

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboard.ui.state.PopupStateMachineV3
import com.davidperi.emojikeyboard.ui.view.EmojiKeyboardView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.updatePadding

class EmojiPopupV3(
    private val context: Context,
    private val rootView: View
) {

    private val keyboardView = EmojiKeyboardView(context)
    private val stateMachine = PopupStateMachineV3(this, rootView)

    private val container: FrameLayout = FrameLayout(context)
    private var popupWindow: PopupWindow? = null
    private val decorView: View? = findActivity()?.window?.decorView

    private var originalPaddingBottom: Int = -1


    init {
        container.addView(keyboardView)
        keyboardView.onSearchBarFocusChange = { stateMachine.onSearchFocusChanged(it) }
    }


    // PUBLIC (external) API
    fun setupWith(editText: EditText) {
        keyboardView.setupWith(editText)
        // stateMachine.setEditText(editText)
    }

    fun setConfig(config: EmojiKeyboardConfig) {
        keyboardView.setConfig(config)
    }

    fun toggle() = stateMachine.toggle()
    fun hide() = stateMachine.hide()

    fun isShowing() = popupWindow?.isShowing ?: false

    fun getState() = stateMachine.state

    fun setOnStateChangedListener(callback: (PopupState) -> Unit) {
        stateMachine.onStateChanged = { state ->
            keyboardView.onStateChanged(state)
            callback(state)
        }
    }


    // Public (internal) API
    internal fun showPopup(height: Int) {
        if (isShowing() || decorView == null) {
            popupWindow?.update(ViewGroup.LayoutParams.MATCH_PARENT, height)
            return
        }

        originalPaddingBottom = decorView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            decorView.updatePadding(bottom = height + systemInsets.bottom)
            insets
        }

        popupWindow = PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT, height, false).apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            animationStyle = 0

            setOnDismissListener {
                ViewCompat.setOnApplyWindowInsetsListener(container, null)
                if (originalPaddingBottom != -1) {
                    decorView.updatePadding(bottom = originalPaddingBottom)
                }
                popupWindow = null
                onPopupDismissed()
            }
        }

        popupWindow?.showAtLocation(decorView, Gravity.BOTTOM, 0, 0)
    }

    internal fun dismissPopup() {
        popupWindow?.dismiss()
    }

    internal fun getSearchContentHeight(): Int {
        return keyboardView.getSearchContentHeight()
    }

    internal fun onPopupDismissed() {
        stateMachine.onDismiss()
    }


    // Aux
    private fun findActivity(): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}