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
import com.davidperi.emojikeyboard.utils.MeasureUtils.dp
import com.davidperi.emojikeyboard.utils.ActivityUtils.getActivity
import com.davidperi.emojikeyboard.utils.ActivityUtils.hideKeyboard
import com.davidperi.emojikeyboard.utils.ActivityUtils.showKeyboard

class EmojiPopup(
    private val emojiKeyboard: EmojiKeyboard,
    private val editText: EditText,
    private val onStateChanged: (PopupState) -> Unit
) {

    enum class PopupState { Collapsed, Behind, Focused, Searching }
    var state: PopupState = PopupState.Collapsed

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
    }


    fun hide() {
        if (state == PopupState.Focused) {
            transitionTo(PopupState.Collapsed)
        }
    }

    fun toggle() {
        when (state) {
            PopupState.Collapsed -> transitionTo(PopupState.Focused)
            PopupState.Behind -> transitionTo(PopupState.Focused)
            PopupState.Focused -> transitionTo(PopupState.Behind)
            PopupState.Searching -> transitionTo(PopupState.Behind)
        }
    }


    private fun transitionTo(newState: PopupState) {
        if (state == newState) return

        val oldState = state
        state = newState

        when (newState) {
            PopupState.Collapsed -> {
                if (oldState == PopupState.Focused) animateSize(0)
                editText.requestFocus()
                emojiKeyboard.hideKeyboard()
                backCallback.isEnabled = false
                emojiKeyboard.topBar.isVisible = true
            }

            PopupState.Behind -> {
                if (oldState == PopupState.Focused) shouldMimicIme = false
                if (oldState != PopupState.Collapsed) animateSize(keyboardHeight)
                editText.showKeyboard()
                backCallback.isEnabled = false
                emojiKeyboard.topBar.isVisible = true
            }

            PopupState.Focused -> {
                if (oldState == PopupState.Behind) shouldMimicIme = false
                animateSize(keyboardHeight)
                editText.requestFocus()
                editText.hideKeyboard()
                backCallback.isEnabled = true
                emojiKeyboard.topBar.isVisible = true
            }

            PopupState.Searching -> {
                animateSize(keyboardHeight + EXTENSION_HEIGHT.dp)
                emojiKeyboard.searchBar.showKeyboard()
                backCallback.isEnabled = false
                emojiKeyboard.topBar.isVisible = false
            }
        }

        onStateChanged(newState)
    }


    private fun setupStaticInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(emojiKeyboard) { v, insets ->
            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val effectiveHeight = (imeInset - navInset).coerceAtLeast(0)

            if (effectiveHeight > 0) {  // ime up
                keyboardHeight = effectiveHeight

                when (state) {
                    PopupState.Collapsed -> transitionTo(PopupState.Behind)
                    PopupState.Focused -> {
                        if (editText.hasFocus()) transitionTo(PopupState.Behind)
                        else if (emojiKeyboard.searchBar.hasFocus()) transitionTo(PopupState.Searching)
                    }
                    else -> {}
                }

            } else {  // ime down
                when (state) {
                    PopupState.Behind -> transitionTo(PopupState.Collapsed)
                    PopupState.Searching -> transitionTo(PopupState.Focused)
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
                            PopupState.Collapsed -> changeSize(effectiveHeight)
                            PopupState.Behind -> changeSize(effectiveHeight)
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
            if (hasFocus && state == PopupState.Searching) {
                transitionTo(PopupState.Behind)
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
