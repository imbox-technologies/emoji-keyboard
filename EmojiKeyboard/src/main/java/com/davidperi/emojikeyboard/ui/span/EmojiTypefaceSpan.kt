package com.davidperi.emojikeyboard.ui.span

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class EmojiTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateMeasureState(p0: TextPaint) {
        apply(p0)
    }

    override fun updateDrawState(p0: TextPaint) {
        apply(p0)
    }

    private fun apply(paint: Paint) {
//        val old = paint.typeface
//        val oldStyle = old?.style ?: 0

        paint.typeface = typeface
    }
}