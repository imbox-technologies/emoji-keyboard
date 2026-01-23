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
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import com.imbox.emojikeyboard.R
import com.imbox.emojikeyboard.utils.EmojiUtils
import com.imbox.emojikeyboard.utils.EmojiUtils.getEmojiInfo

open class EmojiTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    @Px
    open var emojiSize: Float = paint.fontMetrics.descent - paint.fontMetrics.ascent
        protected set

    open var enableDynamicEmojiSize: Boolean = false
        protected set


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

        text = text
    }


    @CallSuper
    override fun setText(text: CharSequence?, type: BufferType?) {
        val builder = SpannableStringBuilder(text ?: "")
        val emojiSpanSize = getEmojiSpanSize(text)
        EmojiUtils.replaceEmojis(builder, emojiSpanSize)
        super.setText(builder, type)
    }


    fun setEmojiSize(@Px pixels: Int, shouldInvalidate: Boolean = true) {
        emojiSize = pixels.toFloat()
        if (shouldInvalidate) {
            text = text
        }
    }

    fun setEmojiSizeRes(@DimenRes res: Int, shouldInvalidate: Boolean = true) {
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

}