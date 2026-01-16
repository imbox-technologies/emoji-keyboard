package com.davidperi.emojikeyboard

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
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
import androidx.core.view.children
import androidx.core.view.isEmpty
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.davidperi.emojikeyboard.data.prefs.PrefsManager
import com.davidperi.emojikeyboard.data.prefs.PrefsManager.Companion.DEFAULT_HEIGHT_DP
import com.davidperi.emojikeyboard.ui.anim.EmojiWindowAnimationCallback
import com.davidperi.emojikeyboard.ui.anim.PopupAnimator
import com.davidperi.emojikeyboard.ui.state.PopupState
import com.davidperi.emojikeyboard.ui.state.PopupStateMachine
import com.davidperi.emojikeyboard.ui.view.EmojiKeyboardView
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp
import com.davidperi.emojikeyboard.utils.DisplayUtils.hideKeyboard
import com.davidperi.emojikeyboard.utils.DisplayUtils.showKeyboard

class EmojiPopup(private val rootView: ViewGroup) {

    constructor(activity: Activity) : this(
        activity.findViewById<ViewGroup>(android.R.id.content)
    )

    private class PopupContainer(context: Context): FrameLayout(context)
    private val context = rootView.context
    private val popupContainer = PopupContainer(context)
    private val emojiKeyboard = EmojiKeyboardView(context)
    private val stateMachine = PopupStateMachine(this)
    private val animator = PopupAnimator(this)
    private val prefs = PrefsManager(context)

    private var isInstalled = false
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
        if (rootView.isEmpty()) return

        rootView.children.forEach { child ->
            if (child is PopupContainer) {
                return
            }
        }

        popupContainer.apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, prefs.lastKeyboardHeight, Gravity.BOTTOM)
            addView(emojiKeyboard, LinearLayout.LayoutParams(MATCH_PARENT, 0))
        }
    }

    private fun setupInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            Log.i("EMOJI Popup", "insets intercepted with state=$state")
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (imeVisible && imeInsets.bottom > 0) {
                stateMachine.imeUp()

                val orientation = context.resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val imeHeight = imeInsets.bottom
                    if (prefs.lastKeyboardHeight == -1 || imeHeight < prefs.lastKeyboardHeight) {
                        prefs.lastKeyboardHeight = imeHeight
                    }
                }
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
    val state: PopupState get() = stateMachine.state
    fun bindTo(editText: EditText) { targetEditText = editText }
    fun setConfig(config: EmojiKeyboardConfig) { emojiKeyboard.setConfig(config) }
    fun toggle() {
        if (!isInstalled) {
            isInstalled = true
            rootView.addView(popupContainer)
        }
        stateMachine.toggle()
    }
    fun hide() = stateMachine.hide()
    fun setOnPopupStateChangedListener(callback: (PopupState) -> Unit) { onPopupStateChange = callback }
    fun setAnimationCallback(callback: EmojiWindowAnimationCallback?) { animator.animationCallback = callback }
    

    // INTERNAL API
    internal fun updatePopupHeight(height: Int, animate: Boolean = false) {
        Log.i("EMOJI Popup", "called popup height with $height")
        if (currentHeight == height) return

        if (animate) {
            if (height > 0) {
                popupContainer.updateLayoutParams {
                    Log.d("EMOJI Anim", "updating height to $height")
                    this.height = height
                }
                translatePopupContainer(height - currentHeight)
                animator.animate(0)

            } else {
                animator.animate(popupContainer.height)
            }
        } else {
            popupContainer.updateLayoutParams {
                Log.d("EMOJI Anim", "updating height to $height")
                this.height = height
            }
            translatePopupContainer(0)
        }

        currentHeight = height
        ViewCompat.requestApplyInsets(rootView)
    }

    internal fun translatePopupContainer(offset: Int) {
        popupContainer.translationY = offset.toFloat()
    }

    internal fun getCurrentOffset(): Float {
        return popupContainer.translationY
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
            Log.i("EMOJI Popup", "showing ime")
            targetEditText?.showKeyboard()
        }
    }

    internal fun hideKeyboard() {
        Log.i("EMOJI Popup", "hiding ime")
        targetEditText?.hideKeyboard()

        val originalSetting = targetEditText?.showSoftInputOnFocus ?: true
        targetEditText?.showSoftInputOnFocus = false
        targetEditText?.requestFocus()
        targetEditText?.showSoftInputOnFocus = originalSetting
    }

}
