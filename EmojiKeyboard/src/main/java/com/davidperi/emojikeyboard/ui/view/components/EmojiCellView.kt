package com.davidperi.emojikeyboard.ui.view.components

import android.R
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.min

class EmojiCellView(context: Context) : AppCompatTextView(context) {

    init {
        gravity = Gravity.CENTER
        includeFontPadding = false
        setTextColor(Color.BLACK)

        val outValue = TypedValue()
        context.theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = when {
            widthMode == MeasureSpec.EXACTLY -> widthSize
            heightMode == MeasureSpec.EXACTLY -> heightSize
            widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.AT_MOST -> heightSize
            else -> min(widthSize, heightSize)
        }

        val squareSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(squareSpec, squareSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val size = min(w, h)
        if (size > 0) {
            val newSize = size * 0.57f
            if (textSize != newSize) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
            }
        }
    }
}