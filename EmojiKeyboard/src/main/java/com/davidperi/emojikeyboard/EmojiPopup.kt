package com.davidperi.emojikeyboard

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.davidperi.emojikeyboard.utils.MeasureUtils.dp

class EmojiPopup(
    private val rootView: View,
    private val emojiKeyboard: EmojiKeyboard,
    private val editText: EditText,
    private val onStatusChanged: (Int) -> Unit
) {
    private var DEBUG_KeyboardUp = 300.dp
    private var DEBUG_KeyboardDown = 0

    private var popupStatus: Int = STATE_COLLAPSED

    companion object {
        const val STATE_COLLAPSED = 0
        const val STATE_BEHIND = 1
        const val STATE_FOCUSED = 2
    }

    init {
        setup()
    }

    private fun setup() {
        changeSize(0)
        setupKeyboardListener()
    }

    fun hide() {
        if (popupStatus == STATE_FOCUSED) {
            setStatus(STATE_COLLAPSED)
            changeSize(DEBUG_KeyboardDown)
        }
    }

    fun show() {
        if (popupStatus == STATE_COLLAPSED) {
            setStatus(STATE_FOCUSED)
            changeSize(DEBUG_KeyboardUp)
        }
    }

    fun toggle() {
        when (popupStatus) {
            STATE_FOCUSED -> {
                setStatus(STATE_BEHIND)
                showKeyboard()
            }

            STATE_BEHIND -> {
                setStatus(STATE_FOCUSED)
                hideKeyboard()
            }

            STATE_COLLAPSED -> {
                setStatus(STATE_FOCUSED)
                changeSize(DEBUG_KeyboardUp)
            }
        }
    }

    private fun setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(emojiKeyboard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (rootView.paddingBottom < systemBars.bottom) {
                emojiKeyboard.updatePadding(bottom = systemBars.bottom)
            }

            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            if (imeInset > 0) {
                DEBUG_KeyboardUp = imeInset - rootView.paddingBottom
                // ime up
                when (popupStatus) {
                    STATE_COLLAPSED,
                    STATE_FOCUSED -> {
                        setStatus(STATE_BEHIND)
                        changeSize(DEBUG_KeyboardUp)
                    }
                }

            } else {
                // ime down
                if (popupStatus == STATE_BEHIND) {
                    setStatus(STATE_COLLAPSED)
                    changeSize(DEBUG_KeyboardDown)
                }
            }

            insets
        }
    }

    private fun setStatus(newStatus: Int) {
        popupStatus = newStatus
        onStatusChanged(newStatus)
    }

    private fun changeSize(size: Int) {
        val lp = emojiKeyboard.layoutParams
        if (lp.height != size) {
            lp.height = size
            emojiKeyboard.layoutParams = lp
        }

        emojiKeyboard.isVisible = size > 0
    }

    private fun hideKeyboard() {
        val imm =
            rootView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun showKeyboard() {
        editText.requestFocus()
        val imm =
            editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

}

//    private fun setupTreeObserver() {
//        rootView.viewTreeObserver.addOnGlobalLayoutListener {
//            val rect = Rect()
//            rootView.getWindowVisibleDisplayFrame(rect)
//
//            val screenHeight = rootView.rootView.height
//            val imeHeight = screenHeight - rect.bottom
//
//            // Log.d("EMOJI", "keyboard_height=$imeHeight")
//        }
//    }