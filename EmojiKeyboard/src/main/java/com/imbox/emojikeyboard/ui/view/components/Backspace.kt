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

package com.imbox.emojikeyboard.ui.view.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toDrawable
import io.github.davidimbox.emojikeyboard.R
import com.imbox.emojikeyboard.ui.view.EmojiDelegate
import com.imbox.emojikeyboard.utils.DisplayUtils.dp

@SuppressLint("ClickableViewAccessibility", "ViewConstructor")
internal class Backspace(context: Context, private val delegate: EmojiDelegate) :
    AppCompatImageView(context) {

    private val handler = Handler(Looper.getMainLooper())
    private val deleteRepeater = object : Runnable {
        override fun run() {
            delegate.onBackspacePressed()
            handler.postDelayed(this, 50L)
        }
    }


    init {
        setImageResource(R.drawable.emjkb_delete)
        scaleType = ScaleType.CENTER_INSIDE
        setPadding(12.dp, 12.dp, 12.dp, 12.dp)
        background = createBackspaceBackground(context)

        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    delegate.onBackspacePressed()
                    handler.postDelayed(deleteRepeater, 400L)
                    v.isPressed = true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(deleteRepeater)
                    v.isPressed = false
                    v.performClick()
                }
            }
            true
        }
    }

    private fun createBackspaceBackground(context: Context): StateListDrawable {
        val pressedShape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(context.getColor(R.color.emjkb_light_gray))
        }

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedShape)
            addState(intArrayOf(), Color.TRANSPARENT.toDrawable())
        }
    }

}