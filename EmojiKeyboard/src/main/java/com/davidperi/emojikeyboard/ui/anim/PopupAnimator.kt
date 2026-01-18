package com.davidperi.emojikeyboard.ui.anim

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.davidperi.emojikeyboard.EmojiPopup

internal class PopupAnimator(
    private val popup: EmojiPopup
) {

    var animationCallback: EmojiWindowAnimationCallback? = null

    private var currentAnimator: ValueAnimator? = null

    fun animate(target: Int) {
        currentAnimator?.cancel()
        animationCallback?.onPrepare()

        val popupHeight = popup.getHeight()
        if (popupHeight == target) {
            animationCallback?.onEnd()
            return
        }

        currentAnimator = ValueAnimator.ofInt(popupHeight, target).apply {
            duration = 250L
            interpolator = LinearOutSlowInInterpolator()

            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                popup.setHeight(value)
                animationCallback?.onProgress(animation.animatedFraction)
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    animationCallback?.onStart(target)
                }

                override fun onAnimationEnd(animation: Animator) {
                    animationCallback?.onEnd()
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    animationCallback?.onEnd()
                    currentAnimator = null
                }
            })

            start()
        }
    }

}