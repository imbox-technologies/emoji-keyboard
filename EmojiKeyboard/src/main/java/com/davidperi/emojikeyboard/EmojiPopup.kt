package com.davidperi.emojikeyboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.davidperi.emojikeyboard.utils.MeasureUtils.dp

class EmojiPopup(
    private val rootView: View,
    private val emojiKeyboard: EmojiKeyboard,
    private val editText: EditText,
    private val onStatusChanged: (Int) -> Unit
) {
    private var keyboardHeight = DEFAULT_HEIGHT.dp
    private var popupStatus: Int = STATE_COLLAPSED
    private var currentAnimator: ValueAnimator? = null

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            hide()
        }
    }

    companion object {
        const val STATE_COLLAPSED = 0
        const val STATE_BEHIND = 1
        const val STATE_FOCUSED = 2
        const val STATE_SEARCHING = 3

        private const val DEFAULT_HEIGHT = 300
        private const val EXTENSION_HEIGHT = 100
        private const val ANIMATION_DURATION = 250L
    }

    init {
        emojiKeyboard.layoutParams.height = 0
        emojiKeyboard.isVisible = false

        setupStaticInsetsListener()
        setupAnimatedInsetsListener()
        setupBackPressHandler()
        setupMsgFocusListener()
        setupSearchBarFocusListener()
    }


    fun hide() {
        if (popupStatus == STATE_FOCUSED) {
            setStatus(STATE_COLLAPSED)
            animateSize(0)
        }
    }

    fun show() {
        if (popupStatus == STATE_COLLAPSED) {
            setStatus(STATE_FOCUSED)
            animateSize(keyboardHeight)
        }
    }

    fun toggle() {
        when (popupStatus) {
            STATE_FOCUSED -> {
                showKeyboard()
            }

            STATE_BEHIND -> {
                setStatus(STATE_FOCUSED)
                hideKeyboard()
            }

            STATE_COLLAPSED -> {
                setStatus(STATE_FOCUSED)
                animateSize(keyboardHeight)
            }

            STATE_SEARCHING -> {
                setStatus(STATE_BEHIND)
                editText.requestFocus()
                emojiKeyboard.topBar.isVisible = true
                animateSize(keyboardHeight)
            }
        }
    }


    private fun setupStaticInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(emojiKeyboard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (rootView.paddingBottom < systemBars.bottom) {
                emojiKeyboard.updatePadding(bottom = systemBars.bottom)
            }

            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val effectiveHeight = (imeInset - rootView.paddingBottom).coerceAtLeast(0)

            if (effectiveHeight > 0) {
                // ime up
                keyboardHeight = effectiveHeight

                if (popupStatus == STATE_COLLAPSED) {
                    setStatus(STATE_BEHIND)
                }

            } else {
                // ime down
                when (popupStatus) {
                    STATE_BEHIND -> setStatus(STATE_COLLAPSED)
                    STATE_SEARCHING -> backToNormalSize()
                }
            }

            insets
        }
    }

    private fun setupAnimatedInsetsListener() {
        ViewCompat.setWindowInsetsAnimationCallback(
            rootView, object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: List<WindowInsetsAnimationCompat?>
                ): WindowInsetsCompat {
                    val imeAnimation = runningAnimations.find {
                        it?.typeMask?.and(WindowInsetsCompat.Type.ime()) != 0
                    }

                    if (imeAnimation != null) {
                        val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        val effectiveHeight = (imeHeight - rootView.paddingBottom).coerceAtLeast(0)

                        when (popupStatus) {
                            STATE_BEHIND -> changeSize(effectiveHeight)
                            STATE_COLLAPSED -> changeSize(effectiveHeight)
                            STATE_FOCUSED -> changeSize(keyboardHeight)
                        }
                    }

                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    val isImeVisible = ViewCompat.getRootWindowInsets(rootView)
                        ?.isVisible(WindowInsetsCompat.Type.ime()) == true

                    if (isImeVisible && popupStatus == STATE_FOCUSED) {
                        setStatus(STATE_BEHIND)
                    }

                    if (!isImeVisible && popupStatus == STATE_SEARCHING) {
                        setStatus(STATE_FOCUSED)
                        silentlyFocusMsg()
                    }

                    if (popupStatus == STATE_COLLAPSED) {
                        emojiKeyboard.isVisible = false
                    }
                }
            })
    }

    private fun setupBackPressHandler() {
        val activity = rootView.context.getActivity()
        if (activity is ComponentActivity) {
            activity.onBackPressedDispatcher.addCallback(activity, backCallback)
        } else {
            Log.e("EMOJI", "Back press handling disabled.")
        }
    }

    private fun setupMsgFocusListener() {
        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                when (popupStatus) {
                    STATE_SEARCHING -> toggle()
                }
            }
        }
    }

    private fun setupSearchBarFocusListener() {
        emojiKeyboard.searchBar.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                when (popupStatus) {
                    STATE_FOCUSED -> {
                        setStatus(STATE_SEARCHING)
                        extendSize()
                    }
                }
            }
        }
    }


    private fun setStatus(newStatus: Int) {
        popupStatus = newStatus
        backCallback.isEnabled = (newStatus == STATE_FOCUSED)
        onStatusChanged(newStatus)
    }

    private fun animateSize(targetHeight: Int) {
        currentAnimator?.cancel()

        val currentHeight = emojiKeyboard.layoutParams.height
        if (currentHeight == targetHeight) return

        currentAnimator = ValueAnimator.ofInt(currentHeight, targetHeight).apply {
            duration = ANIMATION_DURATION
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                emojiKeyboard.updateLayoutParams { height = value }

                if (value > 0 && !emojiKeyboard.isVisible) {
                    emojiKeyboard.isVisible = true
                }
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (targetHeight == 0) {
                        emojiKeyboard.isVisible = false
                    }
                    currentAnimator = null
                }
            })

            start()
        }
    }

    private fun changeSize(size: Int) {
        currentAnimator?.cancel()
        if (size == 0) {
            emojiKeyboard.isVisible = false
        }

        if (emojiKeyboard.layoutParams.height != size){
            emojiKeyboard.updateLayoutParams { height = size }
        }

        if (size > 0 && !emojiKeyboard.isVisible) {
            emojiKeyboard.isVisible = true
        }
    }

    private fun extendSize() {
        emojiKeyboard.topBar.isVisible = false
        animateSize(keyboardHeight + EXTENSION_HEIGHT.dp)
    }

    private fun backToNormalSize() {
        emojiKeyboard.topBar.isVisible = true
        animateSize(keyboardHeight)
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

    private fun silentlyFocusMsg() {
        val prev = editText.showSoftInputOnFocus
        editText.showSoftInputOnFocus = false
        editText.requestFocus()
        editText.postDelayed({
            editText.showSoftInputOnFocus = prev
        }, 50)
    }


    private fun Context.getActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

}
