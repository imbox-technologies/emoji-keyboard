package com.davidperi.emojikeyboard.ui.anim

import android.view.View
import android.view.ViewGroup
import com.davidperi.emojikeyboard.EmojiPopup

fun EmojiPopup.setupEmojiPopupAnimation(viewList: List<View>? = null) {
    fun List<View>.bottom() = this.maxOfOrNull { it.bottom } ?: 0
    fun List<View>.translateY(offset: Float) = this.forEach { it.translationY = offset }

    val targetViews = viewList ?: listOf()

    (targetViews.firstOrNull()?.parent as? ViewGroup)?.clipToPadding = false

    val callback = object : EmojiWindowAnimationCallback {
        var startBottom = 0f
        var endBottom = 0f

        override fun onPrepare() {
            startBottom = targetViews.bottom().toFloat()
        }

        override fun onStart() {
            endBottom = targetViews.bottom().toFloat()
            targetViews.translateY(startBottom - endBottom)
        }

        override fun onProgress(fraction: Float) {
            val offset = lerp(startBottom - endBottom, 0f, fraction)
            targetViews.translateY(offset)
        }
    }

    setEmojiPopupAnimationCallback(callback)
}

fun EmojiPopup.clearEmojiPopupAnimation() {
    setEmojiPopupAnimationCallback(null)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}