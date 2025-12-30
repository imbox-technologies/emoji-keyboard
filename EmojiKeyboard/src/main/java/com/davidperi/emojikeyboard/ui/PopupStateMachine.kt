package com.davidperi.emojikeyboard.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.davidperi.emojikeyboard.ui.EmojiKeyboardView.PopupState
import com.davidperi.emojikeyboard.ui.EmojiKeyboardView.PopupState.COLLAPSED
import com.davidperi.emojikeyboard.ui.EmojiKeyboardView.PopupState.BEHIND
import com.davidperi.emojikeyboard.ui.EmojiKeyboardView.PopupState.FOCUSED
import com.davidperi.emojikeyboard.ui.EmojiKeyboardView.PopupState.SEARCHING
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp
import com.davidperi.emojikeyboard.utils.DisplayUtils.hideKeyboard
import com.davidperi.emojikeyboard.utils.DisplayUtils.showKeyboard
import com.davidperi.emojikeyboard.utils.PrefsManager

internal class PopupStateMachine(
    private val emojiKeyboard: EmojiKeyboardView,
    private val editText: EditText,
) {

    var state: PopupState = COLLAPSED
    var onStateChanged: (PopupState) -> Unit = {}

    private val prefs = PrefsManager(emojiKeyboard.context)

    private var keyboardHeight = prefs.lastKeyboardHeight
    private var currentAnimator: ValueAnimator? = null
    private var shouldMimicIme = true
    private var isDetectingKeyboardHeight = false
    private var pendingState: PopupState? = null
    private var keyboardHeightDetected = false
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val EXTENSION_HEIGHT = 150
        private const val ANIMATION_DURATION = 250L
        private const val KEYBOARD_DETECTION_DELAY = 200L
        private const val KEYBOARD_CLOSE_DELAY = 150L
    }

    init {
        emojiKeyboard.layoutParams.height = 0
        emojiKeyboard.isVisible = false

        setupStaticInsetsListener()
        setupAnimatedInsetsListener()
        setupMsgFocusListener()
    }


    fun hide() {
        if (state == FOCUSED) {
            transitionTo(COLLAPSED)
        }
    }

    fun toggle() {
        when (state) {
            COLLAPSED -> transitionTo(FOCUSED)
            BEHIND -> transitionTo(FOCUSED)
            FOCUSED -> transitionTo(BEHIND)
            SEARCHING -> transitionTo(BEHIND)
        }
    }


    private fun transitionTo(newState: PopupState) {
        if (state == newState) return

        val oldState = state

        val defaultHeight = PrefsManager.DEFAULT_HEIGHT_DP.dp
        if (newState == FOCUSED && keyboardHeight == defaultHeight && !isDetectingKeyboardHeight) {
            isDetectingKeyboardHeight = true
            keyboardHeightDetected = false
            pendingState = newState
            editText.requestFocus()
            editText.showKeyboard()
            return
        }

        state = newState
        onStateChanged(newState)

        when (newState) {
            COLLAPSED -> {
                if (oldState == FOCUSED) animateSize(0)
                editText.requestFocus()
                emojiKeyboard.hideKeyboard()
                emojiKeyboard.topBar.isVisible = true
                emojiKeyboard.rvKeyboard.isVisible = true
                emojiKeyboard.searchResults.isVisible = false
            }

            BEHIND -> {
                if (oldState == FOCUSED) shouldMimicIme = false
                if (oldState != COLLAPSED) animateSize(keyboardHeight)
                editText.showKeyboard()
                emojiKeyboard.topBar.isVisible = true
                emojiKeyboard.rvKeyboard.isVisible = true
                emojiKeyboard.searchResults.isVisible = false
            }

            FOCUSED -> {
                if (oldState == BEHIND) shouldMimicIme = false
                animateSize(keyboardHeight)
                if (oldState != SEARCHING){
                    editText.requestFocus()
                    editText.hideKeyboard()
                }else{
                    silentRequestFocus()
                }

                emojiKeyboard.topBar.isVisible = true
                emojiKeyboard.rvKeyboard.isVisible = true
                emojiKeyboard.searchResults.isVisible = false
                emojiKeyboard.refreshRecentsIfNeeded(newState)
            }

            SEARCHING -> {
                val targetHeight = emojiKeyboard.getSearchContentHeight()
                animateSize(keyboardHeight + targetHeight)
                emojiKeyboard.searchBar.showKeyboard()
                emojiKeyboard.topBar.isVisible = false
                emojiKeyboard.rvKeyboard.isVisible = false
                emojiKeyboard.searchResults.isVisible = true
            }
        }
    }


    private fun setupStaticInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(emojiKeyboard) { v, insets ->
            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val effectiveHeight = (imeInset - navInset).coerceAtLeast(0)

            if (effectiveHeight > 0) {  // ime up
                if (keyboardHeight != effectiveHeight) {
                    keyboardHeight = effectiveHeight
                    prefs.lastKeyboardHeight = effectiveHeight
                    emojiKeyboard.setInternalContentHeight(keyboardHeight)
                }

                if (isDetectingKeyboardHeight && effectiveHeight > 0) {
                    keyboardHeightDetected = true
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        editText.clearFocus()
                        editText.hideKeyboard()
                    }, KEYBOARD_DETECTION_DELAY)
                    return@setOnApplyWindowInsetsListener insets
                }

                if (!isDetectingKeyboardHeight) {
                    when (state) {
                        COLLAPSED -> transitionTo(BEHIND)
                        FOCUSED -> {
                            if (editText.hasFocus()) transitionTo(BEHIND)
                            else if (emojiKeyboard.searchBar.hasFocus()) transitionTo(SEARCHING)
                        }
                        else -> {}
                    }
                }
            } else {  // ime down
                if (isDetectingKeyboardHeight && keyboardHeightDetected) {
                    handler.removeCallbacksAndMessages(null)
                    isDetectingKeyboardHeight = false
                    keyboardHeightDetected = false
                    pendingState?.let { targetState ->
                        val savedState = targetState
                        pendingState = null
                        handler.postDelayed({
                            if (keyboardHeight > 0 && keyboardHeight != PrefsManager.DEFAULT_HEIGHT_DP.dp) {
                                emojiKeyboard.post {
                                    transitionTo(savedState)
                                }
                            }
                        }, KEYBOARD_CLOSE_DELAY)
                    }
                    return@setOnApplyWindowInsetsListener insets
                }

                if (!isDetectingKeyboardHeight) {
                    when (state) {
                        BEHIND -> transitionTo(COLLAPSED)
                        SEARCHING -> transitionTo(FOCUSED)
                        else -> {}
                    }
                }
            }

            insets
        }
    }

    private fun setupAnimatedInsetsListener() {
        ViewCompat.setWindowInsetsAnimationCallback(
            emojiKeyboard, object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: List<WindowInsetsAnimationCompat?>
                ): WindowInsetsCompat {
                    val imeAnimation = runningAnimations.find {
                        it?.typeMask?.and(WindowInsetsCompat.Type.ime()) != 0
                    }

                    if (imeAnimation != null && shouldMimicIme) {
                        val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                        val navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                        val effectiveHeight = (imeHeight - navInset).coerceAtLeast(0)

                        when (state) {
                            COLLAPSED -> changeSize(effectiveHeight)
                            BEHIND -> changeSize(effectiveHeight)
                            else -> {}
                        }
                    }

                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    shouldMimicIme = true
                }
            })
    }

    private fun setupMsgFocusListener() {
        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus && state == SEARCHING) {
                transitionTo(BEHIND)
            }
        }
    }


    private fun animateSize(targetHeight: Int) {
        currentAnimator?.cancel()

        val currentHeight = emojiKeyboard.layoutParams.height
        if (currentHeight == targetHeight) return

        if (targetHeight > 0) {
            emojiKeyboard.setInternalContentHeight(targetHeight)
        }

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

        if (size > 0) {
            emojiKeyboard.setInternalContentHeight(maxOf(size, keyboardHeight))
        }

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

    private fun silentRequestFocus() {
        editText.postDelayed({
            editText.requestFocus()
            //editText.hideKeyboard()
        }, 250)
    }

}
