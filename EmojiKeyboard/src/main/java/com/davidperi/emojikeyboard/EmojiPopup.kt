package com.davidperi.emojikeyboard

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import kotlin.math.roundToInt

class EmojiPopup (
    private val rootView: View,
    private val emojiKeyboard: EmojiKeyboard,
    private val editText: EditText,
) {
    private var DEBUG_KeyboardUp = 965 // 1026
    private var DEBUG_KeyboardDown = 0 // 45

    private var popupStatus: Int = STATE_COLLAPSED

    companion object {
        const val STATE_COLLAPSED = 0
        const val STATE_BEHIND = 2
        const val STATE_FOCUSED = 3
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
            popupStatus = STATE_COLLAPSED
            changeSize(DEBUG_KeyboardDown)
        }
    }

    fun show() {
        if (popupStatus == STATE_COLLAPSED) {
            popupStatus = STATE_FOCUSED
            changeSize(DEBUG_KeyboardUp)
        }
    }

    fun toggle(): Int {
        when (popupStatus) {
            STATE_FOCUSED -> {
                popupStatus = STATE_BEHIND
                showKeyboard()
            }

            STATE_BEHIND -> {
                popupStatus = STATE_FOCUSED
                hideKeyboard()
            }

            STATE_COLLAPSED -> {
                popupStatus = STATE_FOCUSED
                changeSize(DEBUG_KeyboardUp)
            }
        }

        return popupStatus
    }

    private fun setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val imeHeight: Int = if (android.os.Build.VERSION.SDK_INT >= 30) {
                insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            } else {
                insets.systemWindowInsetBottom
            }

            Log.d("EMOJI", "$imeHeight")

            if (imeHeight > 0) {
                // ime up
                if (popupStatus == STATE_COLLAPSED) {
                    popupStatus = STATE_BEHIND
                    changeSize(DEBUG_KeyboardUp)
                }
            } else {
                // ime down
                if (popupStatus == STATE_BEHIND) {
                    popupStatus = STATE_COLLAPSED
                    changeSize(DEBUG_KeyboardDown)
                }
            }

            insets
        }
    }

    private fun changeSize(size: Int) {
        val lp = emojiKeyboard.layoutParams
        lp.height = size
        emojiKeyboard.layoutParams = lp

        emojiKeyboard.isVisible = size > 0
    }

    private fun hideKeyboard() {
        val imm = rootView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun showKeyboard() {
        editText.requestFocus()
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).roundToInt()
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