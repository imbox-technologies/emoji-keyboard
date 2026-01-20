package com.davidperi.emojikeyboard.text

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatEditText
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.utils.EmojiUtils

open class EmojiEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    @Px
    open var emojiSize: Float = paint.fontMetrics.descent - paint.fontMetrics.ascent
        protected set


    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.EmojiEditText)
            try {
                emojiSize = typedArray.getDimension(
                    R.styleable.EmojiEditText_emojiSize,
                    emojiSize
                )
            } finally {
                typedArray.recycle()
            }
        }

        text = text
        super.setHint(getHintSpannable(hint))
    }


    @CallSuper
    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        val emojiSpanSize = emojiSize
        EmojiUtils.replaceEmojis(getText(), emojiSpanSize)
    }


    fun setEmojiSize(@Px pixels: Int, shouldInvalidate: Boolean = true) {
        emojiSize = pixels.toFloat()
        if (shouldInvalidate) {
            text = text
            super.setHint(getHintSpannable(hint))
        }
    }

    fun setEmojiSizeRes(@DimenRes res: Int, shouldInvalidate: Boolean = true) {
        setEmojiSize(resources.getDimensionPixelSize(res), shouldInvalidate)
    }


    private fun getHintSpannable(text: CharSequence?): Spannable? {
        if (text == null) return null
        val spannable = SpannableStringBuilder(text)
        EmojiUtils.replaceEmojis(spannable, emojiSize)
        return spannable
    }

}