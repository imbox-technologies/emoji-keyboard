/*
 * Copyright 2026 - David Periañez and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.imbox.emojikeyboard.ui.anim

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
        }

        override fun onStart(animation: WindowInsetsAnimationCompat, bounds: WindowInsetsAnimationCompat.BoundsCompat): WindowInsetsAnimationCompat.BoundsCompat {
            endBottom = targetViews.bottom().toFloat()
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

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}



