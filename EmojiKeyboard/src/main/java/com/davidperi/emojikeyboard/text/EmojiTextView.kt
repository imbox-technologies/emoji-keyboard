package com.davidperi.emojikeyboard.text

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.CallSuper
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
import com.davidperi.emojikeyboard.utils.EmojiFontManager
import com.davidperi.emojikeyboard.utils.EmojiUtils
import com.davidperi.emojikeyboard.utils.EmojiUtils.getEmojiInfo

open class EmojiTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    var enableDynamicEmojiSize: Boolean = true

    private val emojiTypeface by lazy { EmojiFontManager.getTypeface(context) }
    private var originalTextSize: Float = textSize
    private var emojiSize: Float = textSize
    private var isUpdating = false

    init {
        originalTextSize = textSize
        emojiSize = textSize
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        if (!isUpdating) {
            originalTextSize = textSize
            emojiSize = textSize
        }
    }

    @CallSuper
    override fun setText(text: CharSequence?, type: BufferType?) {
        if (isUpdating || text.isNullOrEmpty()) {
            super.setText(text, type)
            return
        }

        isUpdating = true
        try {
            val builder = SpannableStringBuilder(text)
            val targetSpanSize = calculateStylesAndGetSpanSize(text)
            replaceEmojis(builder, targetSpanSize)
            super.setText(builder, type)
        } finally {
            isUpdating = false
        }
    }


    private fun calculateStylesAndGetSpanSize(text: CharSequence): Float? {
        if (!enableDynamicEmojiSize) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize)
            return emojiSize
        }

        val info = text.getEmojiInfo()

        if (info.isOnlyEmojis) {
            val scaleFactor = when (info.numEmojis) {
                1 -> 2.0f
                2 -> 1.5f
                3 -> 1.2f
                else -> 1.0f
            }

            val jumboSize = originalTextSize * scaleFactor
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, jumboSize)
            return null

        } else {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize)
            return emojiSize
        }
    }

    private fun replaceEmojis(spannable: Spannable, spanSizePx: Float?) {
        val existingSpans = spannable.getSpans(0, spannable.length, EmojiTypefaceSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val matcher = EmojiUtils.EMOJI_PATTERN.matcher(spannable)
        while (matcher.find()) {
            spannable.setSpan(
                EmojiTypefaceSpan(emojiTypeface, spanSizePx),
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }


    fun setEmojiSize(@Px pixels: Int, shouldInvalidate: Boolean = false) {
        emojiSize = pixels.toFloat()
        if (shouldInvalidate) {
            text = text
        }
    }

    fun setEmojiSizeRes(@DimenRes res: Int, shouldInvalidate: Boolean = false) {
        setEmojiSize(resources.getDimensionPixelSize(res), shouldInvalidate)
    }

}