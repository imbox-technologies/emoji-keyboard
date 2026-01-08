package com.davidperi.emojikeyboard.text

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
import com.davidperi.emojikeyboard.utils.EmojiFontManager
import com.davidperi.emojikeyboard.utils.EmojiUtils
import com.davidperi.emojikeyboard.utils.EmojiUtils.getEmojiInfo

open class EmojiTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    @Px
    open var emojiSize: Float = paint.fontMetrics.descent - paint.fontMetrics.ascent
        protected set

    open var enableDynamicEmojiSize: Boolean = false
        protected set


    private val emojiTypeface by lazy { EmojiFontManager.getTypeface(context) }


    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.EmojiTextView)
            try {
                emojiSize = typedArray.getDimension(
                    R.styleable.EmojiTextView_emojiSize,
                    emojiSize
                )

                enableDynamicEmojiSize = typedArray.getBoolean(
                    R.styleable.EmojiTextView_dynamicEmojiSizeEnabled,
                    enableDynamicEmojiSize
                )
            } finally {
                typedArray.recycle()
            }
        }
    }


    @CallSuper
    override fun setText(text: CharSequence?, type: BufferType?) {
        val builder = SpannableStringBuilder(text ?: "")
        val emojiSpanSize = getEmojiSpanSize(text)
        replaceEmojis(builder, emojiSpanSize)
        super.setText(builder, type)
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


    private fun getEmojiSpanSize(text: CharSequence?): Float {
        val defaultSize = emojiSize
        if (!enableDynamicEmojiSize) {
            return defaultSize
        } else {
            val info = text.getEmojiInfo()
            if (!info.isOnlyEmojis) {
                return defaultSize
            } else {
                val scaleFactor = when (info.numEmojis) {
                    1 -> 2.0f
                    2 -> 1.5f
                    3 -> 1.2f
                    else -> 1.0f
                }
                return defaultSize * scaleFactor
            }
        }
    }

    private fun replaceEmojis(spannable: Spannable, spanSizePx: Float?) {
        // TODO: improve or generalize this logic
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

}