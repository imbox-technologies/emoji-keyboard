package com.davidperi.emojikeyboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

@SuppressLint("ClickableViewAccessibility")
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
        setBackgroundResource(android.R.drawable.list_selector_background)

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

}