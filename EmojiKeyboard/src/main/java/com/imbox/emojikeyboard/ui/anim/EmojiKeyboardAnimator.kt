/*
 * Copyright 2026 - IMBox Technologies and contributors
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
import android.view.ViewTreeObserver
import com.imbox.emojikeyboard.EmojiPopup

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
        }

        override fun onStart() {
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (viewTreeObserver.isAlive) {
                        viewTreeObserver.removeOnPreDrawListener(this)
                    }

                    endBottom = targetViews.bottom().toFloat()

                    initialTranslation = startBottom - endBottom
                    targetViews.translateY(initialTranslation)
                    isLayoutReadyForAnimation = true

                    return true
                }
            })
        }

        override fun onProgress(fraction: Float) {
            val offset = lerp(startBottom - endBottom, 0f, fraction)
            targetViews.translateY(offset)
        }

        override fun onEnd() {
            targetViews.translateY(0f)
        }
    }

    emojiPopup.setAnimationCallback(callback)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}