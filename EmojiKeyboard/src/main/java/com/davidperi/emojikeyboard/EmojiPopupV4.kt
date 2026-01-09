package com.davidperi.emojikeyboard

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboard.ui.state.PopupStateMachineV4
import com.davidperi.emojikeyboard.utils.DisplayUtils.hideKeyboard
import com.davidperi.emojikeyboard.utils.DisplayUtils.showKeyboard

class EmojiPopupV4(
    private val context: Context,
) {
    private class Wrapper(context: Context) : LinearLayout(context)  // wraps activity + popup

    val state: PopupState get() = stateMachine.state

    private lateinit var wrapper: Wrapper

    private val activity: Activity? = findActivity()
    private val popupContainer = FrameLayout(context)
    private val emojiKeyboard = FrameLayout(context).apply {
        setBackgroundColor(Color.RED)
        setOnClickListener { stateMachine.search() }
    }
    private val stateMachine = PopupStateMachineV4(this)
    private var onPopupStateChange: ((PopupState) -> Unit)? = null
    private var targetEditText: EditText? = null


    init {
        setupLayout()
        setupInsetsListener()
    }


    private fun setupLayout() {
        val content = activity?.findViewById<ViewGroup>(android.R.id.content) ?: return
        if (content.isEmpty()) return

        val originalContent = content.getChildAt(0)
        if (originalContent is Wrapper) return  // Safeguard: already added

        content.removeView(originalContent)

        popupContainer.apply {
            addView(emojiKeyboard, LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }

        wrapper = Wrapper(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            addView(originalContent, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
            addView(popupContainer, LinearLayout.LayoutParams(MATCH_PARENT, 0))
        }

        content.addView(wrapper)
    }

    private fun setupInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(wrapper) { view, insets ->
            Log.d("EMOJI", "insets intercepted")
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            stateMachine.onImeVisibilityChanged(imeVisible)

            val newInsets: WindowInsetsCompat

            when (stateMachine.state) {
                PopupState.COLLAPSED -> {
                    newInsets = insets
                }
                PopupState.BEHIND -> {
                    newInsets = insets
                }
                PopupState.FOCUSED -> {
                    emojiKeyboard.updatePadding(bottom = sysInsets.bottom)
                    val newSysInsets = Insets.of(sysInsets.left, sysInsets.top, sysInsets.right, 0)
                    newInsets = WindowInsetsCompat.Builder(insets)
                        .setInsets(WindowInsetsCompat.Type.systemBars(), newSysInsets)
                        .build()
                }
                PopupState.SEARCHING -> {
                    popupContainer.updatePadding(bottom = imeInsets.bottom)
                    val newSysInsets = Insets.of(sysInsets.left, sysInsets.top, sysInsets.right, 0)
                    val newImeInsets = Insets.of(imeInsets.left, imeInsets.top, imeInsets.right, 0)
                    newInsets = WindowInsetsCompat.Builder(insets)
                        .setInsets(WindowInsetsCompat.Type.systemBars(), newSysInsets)
                        .setInsets(WindowInsetsCompat.Type.ime(), newImeInsets)
                        .build()
                }
            }

            ViewCompat.onApplyWindowInsets(view, newInsets)
        }
    }

    // PUBLIC API
    fun bindTo(editText: EditText) { targetEditText = editText }
    fun toggle() = stateMachine.toggle()
    fun hide() = stateMachine.hide()
    fun setOnPopupStateChangedListener(callback: (PopupState) -> Unit) { onPopupStateChange = callback }


    internal fun updatePopupHeight(newHeight: Int) {
        Log.d("EMOJI", "updating height=$newHeight")
        if (popupContainer.height != newHeight) {
            popupContainer.updateLayoutParams { height = newHeight }
            ViewCompat.requestApplyInsets(wrapper)
        }
    }

    internal fun popupStateChanged(state: PopupState) {
        // emojiKeyboard.onStateChanged(state)
        onPopupStateChange?.invoke(state)
        // ViewCompat.requestApplyInsets(wrapper)
    }

    internal fun showKeyboard() { targetEditText?.showKeyboard() }
    internal fun hideKeyboard() { targetEditText?.hideKeyboard() }


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