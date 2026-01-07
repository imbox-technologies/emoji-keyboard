package com.davidperi.emojikeyboard.text

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatTextView
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
import com.davidperi.emojikeyboard.utils.EmojiFontManager
import java.util.regex.Pattern

class EmojiTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private val emojiTypeface by lazy {
        EmojiFontManager.getTypeface(context)
    }

    // Temporal regex by gemini
    private val emojiPattern = Pattern.compile(
        "(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70-\uD83C\uDD71]\uFE0F?|[\uD83C\uDD7E-\uD83C\uDD7F]\uFE0F?|[\uD83C\uDD8E]\uFE0F?|[\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]|[\uD83C\uDE01-\uD83C\uDE02]\uFE0F?|[\uD83C\uDE1A]\uFE0F?|[\uD83C\uDE2F]\uFE0F?|[\uD83C\uDE32-\uD83C\uDE3A]\uFE0F?|[\uD83C\uDE50-\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA-\u25AB]\uFE0F?|[\u25B6\u25C0]\uFE0F?|[\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A-\u231B]\uFE0F?|[\u2328]\uFE0F?|[\u23CF]\uFE0F?|[\u23E9-\u23F3]\uFE0F?|[\u23F8-\u23FA]\uFE0F?|\uD83C\uDCCF\uFE0F?)(?:\u200D(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70-\uD83C\uDD71]\uFE0F?|[\uD83C\uDD7E-\uD83C\uDD7F]\uFE0F?|[\uD83C\uDD8E]\uFE0F?|[\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]|[\uD83C\uDE01-\uD83C\uDE02]\uFE0F?|[\uD83C\uDE1A]\uFE0F?|[\uD83C\uDE2F]\uFE0F?|[\uD83C\uDE32-\uD83C\uDE3A]\uFE0F?|[\uD83C\uDE50-\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA-\u25AB]\uFE0F?|[\u25B6\u25C0]\uFE0F?|[\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A-\u231B]\uFE0F?|[\u2328]\uFE0F?|[\u23CF]\uFE0F?|[\u23E9-\u23F3]\uFE0F?|[\u23F8-\u23FA]\uFE0F?|\uD83C\uDCCF\uFE0F?))*"
    )

    private var isUpdating = false

    @CallSuper override fun setText(text: CharSequence?, type: BufferType?) {
        if (isUpdating || text.isNullOrEmpty()) {
            super.setText(text, type)
            return
        }

        isUpdating = true
        try {
            val builder = SpannableStringBuilder(text)
            replaceEmojis(builder)
            super.setText(builder, type)
        } finally {
            isUpdating = false
        }
    }

    private fun replaceEmojis(spannable: Spannable) {
        val existingSpans = spannable.getSpans(0, spannable.length, EmojiTypefaceSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val matcher = emojiPattern.matcher(spannable)
        while (matcher.find()) {
            spannable.setSpan(
                EmojiTypefaceSpan(emojiTypeface),
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

}