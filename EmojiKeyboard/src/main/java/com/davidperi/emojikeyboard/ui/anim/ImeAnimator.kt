package com.davidperi.emojikeyboard.ui.anim

import android.os.Build
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.davidperi.emojikeyboard.utils.lerp

fun ViewGroup.setupKeyboardAnimation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val callback = object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
            var startBottom = 0
            var endBottom = 0

            override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
                    suppressLayout(true)
                    startBottom = paddingBottom
                }
                Log.e("EMOJI ImeAnimation", "ime onPrepare (start=$startBottom)")
            }

            override fun onStart(animation: WindowInsetsAnimationCompat, bounds: WindowInsetsAnimationCompat.BoundsCompat): WindowInsetsAnimationCompat.BoundsCompat {
                endBottom = paddingBottom
                suppressLayout(false)
                Log.e("EMOJI ImeAnimation", "ime onStart (end=$endBottom)")
                updatePadding(bottom = startBottom)
                return bounds
            }

            override fun onProgress(insets: WindowInsetsCompat, runningAnimations: List<WindowInsetsAnimationCompat?>): WindowInsetsCompat {
                val imeAnimation = runningAnimations.find {
                    it?.typeMask?.and(WindowInsetsCompat.Type.ime()) != 0
                } ?: return insets

                val offset = lerp(startBottom, endBottom, imeAnimation.interpolatedFraction)
                updatePadding(bottom = offset.toInt())

                return insets
            }

            override fun onEnd(animation: WindowInsetsAnimationCompat) {
                Log.e("EMOJI ImeAnimation", "ime onEnd")
                suppressLayout(false)
                updatePadding(bottom = endBottom)
            }
        }

        ViewCompat.setWindowInsetsAnimationCallback(this, callback)
    }
}