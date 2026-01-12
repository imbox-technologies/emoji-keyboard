package com.davidperi.emojikeyboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.davidperi.emojikeyboard.data.prefs.PrefsManager
import com.davidperi.emojikeyboard.data.prefs.PrefsManager.Companion.DEFAULT_HEIGHT_DP
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboard.ui.state.PopupStateMachineV4
import com.davidperi.emojikeyboard.ui.view.EmojiKeyboardView
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp
import com.davidperi.emojikeyboard.utils.DisplayUtils.hideKeyboard
import com.davidperi.emojikeyboard.utils.DisplayUtils.showKeyboard

class EmojiPopupV4(
    private val context: Context,
) {

    private class Wrapper(context: Context) : LinearLayout(context)  // wraps activity + popup
    private lateinit var wrapper: Wrapper
    private val popupContainer = FrameLayout(context)

    private val emojiKeyboard = EmojiKeyboardView(context)
    private val stateMachine = PopupStateMachineV4(this)
    private val prefs = PrefsManager(context)
    private var currentAnimator: ValueAnimator? = null

    private var onPopupStateChange: ((PopupState) -> Unit)? = null
    private var targetEditText: EditText? = null
        set(value) {
            field = value
            value?.let { editText ->
                emojiKeyboard.setupWith(editText)
                editText.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        stateMachine.write()
                    }
                }
            }
        }


    init {
        setupLayout()
        setupInsetsListener()
        setupSearchFocusListener()
    }


    private fun setupLayout() {
        val activity = findActivity()
        val content = activity?.findViewById<ViewGroup>(android.R.id.content) ?: return
        if (content.isEmpty()) return

        val originalContent = content.getChildAt(0)
        if (originalContent is Wrapper) return  // Safeguard: already added

        content.removeView(originalContent)

        popupContainer.apply {
            addView(emojiKeyboard, LinearLayout.LayoutParams(MATCH_PARENT, 0))
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

            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (imeVisible && imeInsets.bottom > 0) {
                stateMachine.imeUp()
                prefs.lastKeyboardHeight = imeInsets.bottom
            } else {
                stateMachine.imeDown()
            }

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
                    emojiKeyboard.updatePadding(bottom = 0)
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

    private fun setupSearchFocusListener() {
        emojiKeyboard.onSearchBarFocusChange = { hasFocus ->
            if (hasFocus) {
                stateMachine.search()
            }
        }
    }


    // PUBLIC API
    val state: PopupState get() = stateMachine.state
    fun bindTo(editText: EditText) { targetEditText = editText }
    fun setConfig(config: EmojiKeyboardConfig) { emojiKeyboard.setConfig(config) }
    fun toggle() = stateMachine.toggle()
    fun hide() = stateMachine.hide()
    fun setOnPopupStateChangedListener(callback: (PopupState) -> Unit) { onPopupStateChange = callback }


    // INTERNAL API
    internal fun updatePopupHeight(newHeight: Int, animate: Boolean = false) {
        if (popupContainer.height != newHeight) {
            if (animate) {
                animateHeight(newHeight)
            } else {
                popupContainer.updateLayoutParams { height = newHeight }
                ViewCompat.requestApplyInsets(wrapper)
            }
        }
    }

    internal fun popupStateChanged(state: PopupState) {
        emojiKeyboard.onStateChanged(state)
        onPopupStateChange?.invoke(state)
    }

    internal fun getSearchContentHeight(): Int {
        return emojiKeyboard.getSearchContentHeight()
    }

    internal fun getKeyboardStandardHeight(): Int {
        return if (prefs.lastKeyboardHeight != -1) {
            emojiKeyboard.updateContentHeight(prefs.lastKeyboardHeight)
            prefs.lastKeyboardHeight
        } else {
            emojiKeyboard.updateContentHeight(DEFAULT_HEIGHT_DP.dp)
            DEFAULT_HEIGHT_DP.dp
        }
    }

    internal fun showKeyboard() {
        if (state != PopupState.SEARCHING) {
            targetEditText?.showKeyboard()
        }
    }

    internal fun hideKeyboard() {
        targetEditText?.hideKeyboard()

        val originalSetting = targetEditText?.showSoftInputOnFocus ?: true
        targetEditText?.showSoftInputOnFocus = false
        targetEditText?.requestFocus()
        targetEditText?.showSoftInputOnFocus = originalSetting
    }


    // AUX
    private fun animateHeight(targetHeight: Int) {
        currentAnimator?.cancel()

        val currentHeight = popupContainer.height

        currentAnimator = ValueAnimator.ofInt(currentHeight, targetHeight).apply {
            duration = 250L
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                popupContainer.updateLayoutParams { height = value }
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })

            start()
        }
    }

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
