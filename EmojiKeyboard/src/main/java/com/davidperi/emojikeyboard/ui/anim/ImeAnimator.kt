package com.davidperi.emojikeyboard.ui.anim

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

fun ViewGroup.setupKeyboardAnimation(viewList: List<View>? = null) {
    fun List<View>.bottom() = this.maxOfOrNull { it.bottom } ?: 0
    fun List<View>.translateY(offset: Float) = this.forEach { it.translationY = offset }

    clipToPadding = false
    val targetViews = if (viewList.isNullOrEmpty()) listOf(this) else viewList

    val callback = object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
        var startBottom = 0f
        var endBottom = 0f

        override fun onPrepare(animation: WindowInsetsAnimationCompat) {
            startBottom = targetViews.bottom().toFloat()
            Log.d("ANIM", "ime start $startBottom")
        }

        override fun onStart(animation: WindowInsetsAnimationCompat, bounds: WindowInsetsAnimationCompat.BoundsCompat): WindowInsetsAnimationCompat.BoundsCompat {
            endBottom = targetViews.bottom().toFloat()
            Log.d("ANIM", "ime end   $endBottom")
            targetViews.translateY(startBottom - endBottom)
            return bounds
        }

        override fun onProgress(insets: WindowInsetsCompat, runningAnimations: List<WindowInsetsAnimationCompat?>): WindowInsetsCompat {
            val imeAnimation = runningAnimations.find {
                it?.typeMask?.and(WindowInsetsCompat.Type.ime()) != 0
            } ?: return insets

            val offset = lerp(startBottom - endBottom, 0f, imeAnimation.interpolatedFraction)
            targetViews.translateY(offset)

            return insets
        }

        override fun onEnd(animation: WindowInsetsAnimationCompat) {
            targetViews.translateY(0f)
        }
    }

    ViewCompat.setWindowInsetsAnimationCallback(this, callback)
}

fun View.clearKeyboardAnimation() {
    ViewCompat.setWindowInsetsAnimationCallback(this, null)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}



