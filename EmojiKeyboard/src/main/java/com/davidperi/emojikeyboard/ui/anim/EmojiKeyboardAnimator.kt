package com.davidperi.emojikeyboard.ui.anim

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.davidperi.emojikeyboard.EmojiPopup

fun ViewGroup.setupEmojiPopupAnimation(emojiPopup: EmojiPopup, viewList: List<View>? = null) {
    fun List<View>.bottom() = this.maxOfOrNull { it.bottom } ?: 0
    fun List<View>.translateY(offset: Float) = this.forEach { it.translationY = offset }

    clipToPadding = false
    val targetViews = if (viewList.isNullOrEmpty()) listOf(this) else viewList

    val callback = object : EmojiWindowAnimationCallback {
        var startBottom = 0f
        var endBottom = 0f
        var initialTranslation = 0f

        var isLayoutReadyForAnimation = false

        override fun onPrepare() {
            startBottom = targetViews.bottom().toFloat()
            Log.d("EMOJI Animt", "key start $startBottom")
        }

        override fun onStart() {
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (viewTreeObserver.isAlive) {
                        viewTreeObserver.removeOnPreDrawListener(this)
                    }

                    endBottom = targetViews.bottom().toFloat()
                    Log.d("ANIM", "key end   $endBottom")

                    initialTranslation = startBottom - endBottom
                    targetViews.translateY(initialTranslation)
                    isLayoutReadyForAnimation = true

                    return true
                }
            })
        }

        override fun onProgress(fraction: Float) {
            Log.d("EMOJI Animt", "progress   $fraction")
            val offset = lerp(startBottom - endBottom, 0f, fraction)
            targetViews.translateY(offset)
        }

        override fun onEnd() {
            targetViews.translateY(0f)
        }
    }

    emojiPopup.setAnimationCallback(callback)
}

fun ViewGroup.clearEmojiPopupAnimation(emojiPopup: EmojiPopup) {
    emojiPopup.setAnimationCallback(null)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}