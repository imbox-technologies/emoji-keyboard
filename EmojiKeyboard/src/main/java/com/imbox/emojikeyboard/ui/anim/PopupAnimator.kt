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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.imbox.emojikeyboard.EmojiPopup

internal class PopupAnimator(
    private val popup: EmojiPopup
) {

    var animationCallback: EmojiWindowAnimationCallback? = null

    private var currentAnimator: ValueAnimator? = null

    fun animate(target: Int) {
        animationCallback?.onPrepare()
        currentAnimator?.cancel()

        val currentOffset = popup.getCurrentOffset()
        if (currentOffset == target.toFloat()) {
            animationCallback?.onEnd()
            return
        }

        currentAnimator = ValueAnimator.ofInt(currentOffset.toInt(), target).apply {
            duration = 250L
            interpolator = LinearOutSlowInInterpolator()

            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                popup.translatePopupContainer(value)
                animationCallback?.onProgress(animation.animatedFraction)
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    animationCallback?.onStart()
                }

                override fun onAnimationEnd(animation: Animator) {
                    animationCallback?.onEnd()
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    animationCallback?.onEnd()
                }
            })

            start()
        }
    }

}