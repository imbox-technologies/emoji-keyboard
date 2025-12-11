package com.davidperi.emojikeyboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.davidperi.emojikeyboard.ui.EmojiKeyboard
import com.davidperi.emojikeyboard.utils.MeasureUtils.dp
import com.davidperi.emojikeyboard.ui.EmojiKeyboard.PopupState
import com.davidperi.emojikeyboard.ui.EmojiKeyboard.PopupState.COLLAPSED
import com.davidperi.emojikeyboard.ui.EmojiKeyboard.PopupState.BEHIND
import com.davidperi.emojikeyboard.ui.EmojiKeyboard.PopupState.FOCUSED
import com.davidperi.emojikeyboard.ui.EmojiKeyboard.PopupState.SEARCHING
import com.davidperi.emojikeyboard.utils.ActivityUtils.getActivity
import com.davidperi.emojikeyboard.utils.ActivityUtils.hideKeyboard
import com.davidperi.emojikeyboard.utils.ActivityUtils.showKeyboard

internal class EmojiPopup(
    private val emojiKeyboard: EmojiKeyboard,
    private val editText: EditText,
) {

    var state: PopupState = COLLAPSED

    private var keyboardHeight = DEFAULT_HEIGHT.dp
    private var currentAnimator: ValueAnimator? = null
    private var shouldMimicIme = true

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            hide()
        }
    }

    companion object {
        private const val DEFAULT_HEIGHT = 300
        private const val EXTENSION_HEIGHT = 150
        private const val ANIMATION_DURATION = 250L
    }

    init {
        emojiKeyboard.layoutParams.height = 0
        emojiKeyboard.isVisible = false

        setupStaticInsetsListener()
        setupAnimatedInsetsListener()
        setupBackPressHandler()
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
        state = newState

        when (newState) {
            COLLAPSED -> {
                if (oldState == FOCUSED) animateSize(0)
                editText.requestFocus()
                emojiKeyboard.hideKeyboard()
                backCallback.isEnabled = false
                emojiKeyboard.topBar.isVisible = true
            }

            BEHIND -> {
                if (oldState == FOCUSED) shouldMimicIme = false
                if (oldState != COLLAPSED) animateSize(keyboardHeight)
                editText.showKeyboard()
                backCallback.isEnabled = false
                emojiKeyboard.topBar.isVisible = true
            }

            FOCUSED -> {
                if (oldState == BEHIND) shouldMimicIme = false
                animateSize(keyboardHeight)
                editText.requestFocus()
                editText.hideKeyboard()
                backCallback.isEnabled = true
                emojiKeyboard.topBar.isVisible = true
            }

            SEARCHING -> {
                animateSize(keyboardHeight + EXTENSION_HEIGHT.dp)
                emojiKeyboard.searchBar.showKeyboard()
                backCallback.isEnabled = false
                emojiKeyboard.topBar.isVisible = false
            }
        }
    }


    private fun setupStaticInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(emojiKeyboard) { v, insets ->
            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val effectiveHeight = (imeInset - navInset).coerceAtLeast(0)

            if (effectiveHeight > 0) {  // ime up
                keyboardHeight = effectiveHeight

                when (state) {
                    COLLAPSED -> transitionTo(BEHIND)
                    FOCUSED -> {
                        if (editText.hasFocus()) transitionTo(BEHIND)
                        else if (emojiKeyboard.searchBar.hasFocus()) transitionTo(SEARCHING)
                    }
                    else -> {}
                }

            } else {  // ime down
                when (state) {
                    BEHIND -> transitionTo(COLLAPSED)
                    SEARCHING -> transitionTo(FOCUSED)
                    else -> {}
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

    private fun setupBackPressHandler() {
        val activity = emojiKeyboard.context.getActivity()
        if (activity is ComponentActivity) {
            activity.onBackPressedDispatcher.addCallback(activity, backCallback)
        }
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

}
