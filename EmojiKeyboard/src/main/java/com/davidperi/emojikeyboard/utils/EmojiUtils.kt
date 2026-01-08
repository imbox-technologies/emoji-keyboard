package com.davidperi.emojikeyboard.utils

import android.content.Context
import android.text.Spannable
import com.davidperi.emojikeyboard.data.model.EmojiInfo
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
import java.util.regex.Pattern

object EmojiUtils {

    val EMOJI_PATTERN: Pattern = Pattern.compile(
        "(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70-\uD83C\uDD71]\uFE0F?|[\uD83C\uDD7E-\uD83C\uDD7F]\uFE0F?|[\uD83C\uDD8E]\uFE0F?|[\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]|[\uD83C\uDE01-\uD83C\uDE02]\uFE0F?|[\uD83C\uDE1A]\uFE0F?|[\uD83C\uDE2F]\uFE0F?|[\uD83C\uDE32-\uD83C\uDE3A]\uFE0F?|[\uD83C\uDE50-\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA-\u25AB]\uFE0F?|[\u25B6\u25C0]\uFE0F?|[\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A-\u231B]\uFE0F?|[\u2328]\uFE0F?|[\u23CF]\uFE0F?|[\u23E9-\u23F3]\uFE0F?|[\u23F8-\u23FA]\uFE0F?|\uD83C\uDCCF\uFE0F?)(?:\u200D(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70-\uD83C\uDD71]\uFE0F?|[\uD83C\uDD7E-\uD83C\uDD7F]\uFE0F?|[\uD83C\uDD8E]\uFE0F?|[\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]|[\uD83C\uDE01-\uD83C\uDE02]\uFE0F?|[\uD83C\uDE1A]\uFE0F?|[\uD83C\uDE2F]\uFE0F?|[\uD83C\uDE32-\uD83C\uDE3A]\uFE0F?|[\uD83C\uDE50-\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA-\u25AB]\uFE0F?|[\u25B6\u25C0]\uFE0F?|[\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A-\u231B]\uFE0F?|[\u2328]\uFE0F?|[\u23CF]\uFE0F?|[\u23E9-\u23F3]\uFE0F?|[\u23F8-\u23FA]\uFE0F?|\uD83C\uDCCF\uFE0F?))*"
    )

    // TODO: optimize this
    fun CharSequence?.getEmojiInfo(): EmojiInfo {
        if (this.isNullOrEmpty()) {
            return EmojiInfo(isOnlyEmojis = false, numEmojis = 0)
        }

        val matcher = EMOJI_PATTERN.matcher(this)
        var count = 0
        var lastMatchEnd = 0
        var hasNonEmojiContent = false

        while (matcher.find()) {
            if (this.subSequence(lastMatchEnd, matcher.start()).isNotBlank()) {
                hasNonEmojiContent = true
            }

            count++
            lastMatchEnd = matcher.end()
        }

        if (this.subSequence(lastMatchEnd, this.length).isNotBlank()) {
            hasNonEmojiContent = true
        }

        if (count == 0) {
            hasNonEmojiContent = true
        }

        return EmojiInfo(
            isOnlyEmojis = !hasNonEmojiContent,
            numEmojis = count
        )
    }

    // TODO: improve or generalize this logic
    fun replaceEmojis(context: Context, spannable: Spannable?, spanSizePx: Float?) {
        if (spannable == null) return

        val typeface = EmojiFontManager.getTypeface(context)
        val existingSpans = spannable.getSpans(0, spannable.length, EmojiTypefaceSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val matcher = EMOJI_PATTERN.matcher(spannable)
        while (matcher.find()) {
            spannable.setSpan(
                EmojiTypefaceSpan(typeface, spanSizePx),
                matcher.start(),
                matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

}