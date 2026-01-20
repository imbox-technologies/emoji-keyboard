package com.davidperi.emojikeyboard.ui.view.components

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
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.ui.view.EmojiDelegate
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

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
        setImageResource(R.drawable.delete)
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
            setColor(context.getColor(R.color.emoji_keyboard_light_gray))
        }

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedShape)
            addState(intArrayOf(), Color.TRANSPARENT.toDrawable())
        }
    }

}