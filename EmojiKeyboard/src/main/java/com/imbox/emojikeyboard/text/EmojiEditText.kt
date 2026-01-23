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

package com.imbox.emojikeyboard.text

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatEditText
import io.github.davidimbox.emojikeyboard.R
import com.imbox.emojikeyboard.utils.EmojiUtils

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