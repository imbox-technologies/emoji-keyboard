package com.davidperi.emojikeyboard

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.Gravity
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
import com.davidperi.emojikeyboard.data.prefs.PrefsManager
import com.davidperi.emojikeyboard.data.prefs.PrefsManager.Companion.DEFAULT_HEIGHT_DP
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboard.ui.state.PopupStateMachine
import com.davidperi.emojikeyboard.ui.view.EmojiKeyboardView
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp
import com.davidperi.emojikeyboard.utils.DisplayUtils.hideKeyboard
import com.davidperi.emojikeyboard.utils.DisplayUtils.showKeyboard
import kotlin.contracts.contract

class EmojiPopupV6 private constructor(
    private val context: Context,
) : PopupApi, InternalPopup {

    companion object {
        operator fun invoke(context: Context): PopupApi {
            return EmojiPopupV6(context)
        }
    }

    private val contentView = findActivity()?.findViewById<ViewGroup>(android.R.id.content)

    private class PopupContainer(context: Context): FrameLayout(context)
    private val popupContainer = PopupContainer(context)
    private val emojiKeyboard = EmojiKeyboardView(context)
    private val stateMachine = PopupStateMachine(this)
    private val prefs = PrefsManager(context)

    private var currentHeight = 0
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
        if (contentView == null || contentView.isEmpty()) return

        val originalContent = contentView.getChildAt(0)
        if (originalContent is PopupContainer) return  // Safeguard: already added

        popupContainer.apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, prefs.lastKeyboardHeight, Gravity.BOTTOM)
            addView(emojiKeyboard, LinearLayout.LayoutParams(MATCH_PARENT, 0))
        }

        contentView.addView(popupContainer)
    }

    private fun setupInsetsListener() {
        if (contentView == null) return
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, insets ->
            Log.i("EMOJI Popup", "insets intercepted with state=$state")
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
                    val newImeInsets = Insets.of(imeInsets.left, imeInsets.top, imeInsets.right, currentHeight)
                    newInsets = WindowInsetsCompat.Builder(insets)
                        .setInsets(WindowInsetsCompat.Type.ime(), newImeInsets)
                        .build()
                }
                PopupState.SEARCHING -> {
                    emojiKeyboard.updatePadding(bottom = 0)
                    val newImeInsets = Insets.of(imeInsets.left, imeInsets.top, imeInsets.right, currentHeight)
                    newInsets = WindowInsetsCompat.Builder(insets)
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
    override val state: PopupState get() = stateMachine.state
    override fun bindTo(editText: EditText) { targetEditText = editText }
    override fun setConfig(config: EmojiKeyboardConfig) { emojiKeyboard.setConfig(config) }
    override fun toggle() = stateMachine.toggle()
    override fun hide() = stateMachine.hide()
    override fun setOnPopupStateChangedListener(callback: (PopupState) -> Unit) { onPopupStateChange = callback }


    // INTERNAL API
    override fun updatePopupHeight(height: Int) {
        if (popupContainer.height != height) {
            Log.i("EMOJI Popup", "updating height")
            currentHeight = height
            popupContainer.updateLayoutParams { this.height = height }
            contentView?.let { ViewCompat.requestApplyInsets(contentView) }
        }
    }

    override fun popupStateChanged(state: PopupState) {
        emojiKeyboard.onStateChanged(state)
        onPopupStateChange?.invoke(state)
    }

    override fun getSearchContentHeight(): Int {
        return emojiKeyboard.getSearchContentHeight()
    }

    override fun getKeyboardStandardHeight(): Int {
        return if (prefs.lastKeyboardHeight != -1) {
            emojiKeyboard.updateContentHeight(prefs.lastKeyboardHeight)
            prefs.lastKeyboardHeight
        } else {
            emojiKeyboard.updateContentHeight(DEFAULT_HEIGHT_DP.dp)
            DEFAULT_HEIGHT_DP.dp
        }
    }

    override fun showKeyboard() {
        if (state != PopupState.SEARCHING) {
            Log.i("EMOJI Popup", "showing ime")
            targetEditText?.showKeyboard()
        }
    }

    override fun hideKeyboard() {
        Log.i("EMOJI Popup", "hiding ime")
        targetEditText?.hideKeyboard()

        val originalSetting = targetEditText?.showSoftInputOnFocus ?: true
        targetEditText?.showSoftInputOnFocus = false
        targetEditText?.requestFocus()
        targetEditText?.showSoftInputOnFocus = originalSetting
    }


    // AUX
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
