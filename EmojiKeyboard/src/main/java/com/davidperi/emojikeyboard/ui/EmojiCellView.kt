package com.davidperi.emojikeyboard.ui

import android.content.Context
import android.icu.util.Measure
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.min

class EmojiCellView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    var isHorizontalMode: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)

        val finalSize: Int = if (isHorizontalMode) {
            parentHeight // measuredHeight
        } else {
            parentWidth // measuredWidth
        }

        val squareSpec = MeasureSpec.makeMeasureSpec(finalSize, MeasureSpec.EXACTLY)
        super.onMeasure(squareSpec, squareSpec)

        setMeasuredDimension(finalSize, finalSize)
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