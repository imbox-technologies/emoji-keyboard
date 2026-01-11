package com.davidperi.emojikeyboard

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VERTICAL
import androidx.activity.ComponentActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboard.ui.state.PopupStateMachineV2
import com.davidperi.emojikeyboard.ui.view.EmojiKeyboardView

class EmojiPopupV2(activity: ComponentActivity) {

    private class ScreenContainer(context: Context) : LinearLayout(context)
    private class PopupContainer(context: Context) : FrameLayout(context)

    private val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

    private var screenContainer: ScreenContainer? = null
    private var popupContainer: PopupContainer? = null
    private var userView: View? = null

    private var stateMachine: PopupStateMachineV2? = null
    private val keyboardView = EmojiKeyboardView(activity)

    private var currentEmojiHeight = 0
    private var isEmojiVisible = false

    init {
        wrapKeyboardView(activity)
        install(activity)
    }


    // PUBLIC (external) API
    fun setupWith(editText: EditText) {
        keyboardView.setupWith(editText)
        stateMachine = PopupStateMachineV2(this, editText)
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
        keyboardView.updateContentHeight(height)
    }

    internal fun isSearchFocused(): Boolean {
        return keyboardView.isSearchFocused()
    }

    internal fun updatePopupLayoutHeight(newHeight: Int) {
        popupContainer?.updateLayoutParams { height = newHeight }
        currentEmojiHeight = newHeight
        isEmojiVisible = newHeight > 0
        screenContainer?.let { ViewCompat.requestApplyInsets(it) }
    }


    // Decorator framing install
    private fun wrapKeyboardView(activity: ComponentActivity) {
        popupContainer = PopupContainer(activity).apply {
            addView(keyboardView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }

    private fun install(activity: ComponentActivity) {
        val userContent = rootView.getChildAt(0) ?: return
        if (userContent is ScreenContainer) return  // already installed

        rootView.removeView(userContent)
        userView = userContent

        val sc = buildScreenContainer(activity, userContent, popupContainer!!)
        screenContainer = sc
        rootView.addView(sc)

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                stateMachine = null
                screenContainer = null
                popupContainer = null
                userView = null
            }
        })
    }

    private fun buildScreenContainer(context: Context, userView: View, emojiView: View): ScreenContainer {
        val containerView = ScreenContainer(context).apply {
            orientation = VERTICAL
            addView(userView, LayoutParams(MATCH_PARENT, 0, 1f))
            addView(emojiView, LayoutParams(MATCH_PARENT, 0))
        }
        setupWindowInsetsHandling(containerView)
        return containerView
    }

    private fun setupWindowInsetsHandling(containerView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(containerView) { _, insets ->
            handleApplyWindowInsets(insets)
        }
    }

    private fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        val targetView = userView ?: return insets

        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        val actualImeHeight = (imeInsets.bottom - sysInsets.bottom).coerceAtLeast(0)

        val finalInsetBottom = if (actualImeHeight > 0) {
            imeInsets.bottom
        } else if (isEmojiVisible && currentEmojiHeight > 0) {
            currentEmojiHeight + sysInsets.bottom
        } else {
            imeInsets.bottom
        }

        val newImeInsets = Insets.of(imeInsets.left, imeInsets.top, imeInsets.right, finalInsetBottom)

        val newInsets = WindowInsetsCompat.Builder(insets)
            .setInsets(WindowInsetsCompat.Type.ime(), newImeInsets)
            .build()

        ViewCompat.dispatchApplyWindowInsets(targetView, newInsets)
        return insets
    }

}