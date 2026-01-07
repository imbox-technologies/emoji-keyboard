package com.davidperi.emojikeyboard.ui.state

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import android.widget.EditText
import com.davidperi.emojikeyboard.EmojiPopupV2
import com.davidperi.emojikeyboard.data.prefs.PrefsManager
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp
import com.davidperi.emojikeyboard.utils.DisplayUtils.hideKeyboard
import com.davidperi.emojikeyboard.utils.DisplayUtils.showKeyboard

internal class PopupStateMachineV2(
    private val popup: EmojiPopupV2,
    private val editText: EditText,
) {

    var state: PopupState = PopupState.COLLAPSED
    var onStateChanged: (PopupState) -> Unit = {}

    private val prefs = PrefsManager(editText.context)

    private var keyboardHeight = prefs.lastKeyboardHeight
    private var lastAppliedHeight = 0

    private var currentAnimator: ValueAnimator? = null
    private var shouldMimicIme = true
    private var isDetectingKeyboardHeight = false
    private var pendingState: PopupState? = null
    private var keyboardHeightDetected = false
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val ANIMATION_DURATION = 250L
        private const val KEYBOARD_DETECTION_DELAY = 200L
        private const val KEYBOARD_CLOSE_DELAY = 150L
    }

    init {
        updateHeight(0)

        setupStaticInsetsListener()
        setupAnimatedInsetsListener()
        setupMsgFocusListener()
    }

    fun hide() {
        if (state == PopupState.FOCUSED) {
            transitionTo(PopupState.COLLAPSED)
        }
    }

    fun toggle() {
        when (state) {
            PopupState.COLLAPSED -> transitionTo(PopupState.FOCUSED)
            PopupState.BEHIND -> transitionTo(PopupState.FOCUSED)
            PopupState.FOCUSED -> transitionTo(PopupState.BEHIND)
            PopupState.SEARCHING -> transitionTo(PopupState.BEHIND)
        }
    }

    private fun transitionTo(newState: PopupState) {
        if (state == newState) return

        val oldState = state
        val defaultHeight = PrefsManager.Companion.DEFAULT_HEIGHT_DP.dp

        if (newState == PopupState.FOCUSED && keyboardHeight == defaultHeight && !isDetectingKeyboardHeight) {
            isDetectingKeyboardHeight = true
            keyboardHeightDetected = false
            pendingState = newState
            editText.requestFocus()
            editText.showKeyboard()
            return
        }

        state = newState
        onStateChanged(newState)
        popup.notifyStateChanged(newState)

        when (newState) {
            PopupState.COLLAPSED -> {
                if (oldState == PopupState.FOCUSED) animateSize(0)
                editText.requestFocus()
                editText.hideKeyboard()
            }

            PopupState.BEHIND -> {
                if (oldState == PopupState.FOCUSED) shouldMimicIme = false
                if (oldState != PopupState.COLLAPSED) animateSize(keyboardHeight)
                editText.showKeyboard()
            }

            PopupState.FOCUSED -> {
                if (oldState == PopupState.BEHIND) shouldMimicIme = false
                animateSize(keyboardHeight)
                if (oldState != PopupState.SEARCHING){
                    editText.requestFocus()
                    editText.hideKeyboard()
                } else {
                    silentRequestFocus()
                }
            }

            PopupState.SEARCHING -> {
                val targetHeight = popup.getSearchContentHeight()
                animateSize(keyboardHeight + targetHeight)
            }
        }
    }

    private fun setupStaticInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(editText) { _, insets ->
            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val effectiveHeight = (imeInset - navInset).coerceAtLeast(0)

            if (effectiveHeight > 0) {  // ime up
                if (keyboardHeight != effectiveHeight) {
                    keyboardHeight = effectiveHeight
                    prefs.lastKeyboardHeight = effectiveHeight
                    popup.setInternalHeight(keyboardHeight)
                }

                if (isDetectingKeyboardHeight) {
                    keyboardHeightDetected = true
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        editText.clearFocus()
                        editText.hideKeyboard()
                    }, KEYBOARD_DETECTION_DELAY)

                } else {
                    when (state) {
                        PopupState.COLLAPSED -> transitionTo(PopupState.BEHIND)
                        PopupState.FOCUSED -> {
                            if (editText.hasFocus()) transitionTo(PopupState.BEHIND)
                            else if (popup.isSearchFocused()) transitionTo(PopupState.SEARCHING)
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
                            if (keyboardHeight > 0) transitionTo(savedState)
                        }, KEYBOARD_CLOSE_DELAY)
                    }
                }

                if (!isDetectingKeyboardHeight) {
                    when (state) {
                        PopupState.BEHIND -> transitionTo(PopupState.COLLAPSED)
                        PopupState.SEARCHING -> transitionTo(PopupState.FOCUSED)
                        else -> {}
                    }
                }
            }

            insets
        }
    }

    private fun setupAnimatedInsetsListener() {
        ViewCompat.setWindowInsetsAnimationCallback(
            editText, object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
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
                            PopupState.COLLAPSED -> changeSize(effectiveHeight)
                            PopupState.BEHIND -> changeSize(effectiveHeight)
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
            if (hasFocus && state == PopupState.SEARCHING) {
                transitionTo(PopupState.BEHIND)
            }
        }
    }

    private fun animateSize(targetHeight: Int) {
        currentAnimator?.cancel()

        val currentHeight = lastAppliedHeight
        if (currentHeight == targetHeight) return

        if (targetHeight > 0) {
            popup.setInternalHeight(targetHeight)
        }

        currentAnimator = ValueAnimator.ofInt(currentHeight, targetHeight).apply {
            duration = ANIMATION_DURATION
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                updateHeight(value)
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }
            })

            start()
        }
    }

    private fun changeSize(size: Int) {
        currentAnimator?.cancel()

        if (size > 0) {
            popup.setInternalHeight(maxOf(size, keyboardHeight))
        }

        if (lastAppliedHeight != size){
            updateHeight(size)
        }
    }

    private fun updateHeight(height: Int) {
        lastAppliedHeight = height
        popup.updatePopupLayoutHeight(height)
    }

    private fun silentRequestFocus() {
        editText.postDelayed({
            editText.requestFocus()
        }, 250)
    }
}