package com.davidperi.emojikeyboard.utils

import android.text.Spannable
import com.davidperi.emojikeyboard.EmojiManager
import com.davidperi.emojikeyboard.data.EmojiIndex
import com.davidperi.emojikeyboard.data.model.EmojiInfo
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
import java.text.BreakIterator

object EmojiUtils {

    fun isEmoji(chunk: CharSequence): Boolean {
        if (!EmojiManager.isInstalled()) return false
        return EmojiManager.getEmojiIndex().contains(chunk.toString())
    }

    fun splitByGraphemes(text: CharSequence): List<IntRange> {
        if (!EmojiManager.isInstalled()) return emptyList()

        val result = mutableListOf<IntRange>()
        forEachGrapheme(text, EmojiManager.getEmojiIndex()) { _, start, end, isEmoji ->
            if (isEmoji) {
                result.add(start until end)
            }
        }
        return result
    }

    fun CharSequence?.getEmojiInfo(): EmojiInfo {
        if (this.isNullOrEmpty() || !EmojiManager.isInstalled()) {
            return EmojiInfo(isOnlyEmojis = false, numEmojis = 0)
        }

        var count = 0
        var hasNonEmojiContent = false

        forEachGrapheme(this, EmojiManager.getEmojiIndex()) { grapheme, _, _, isEmoji ->
            if (isEmoji) {
                count++
            } else if (grapheme.isNotBlank()) {
                hasNonEmojiContent = true
            }
        }

        if (count == 0) {
            hasNonEmojiContent = true
        }

        return EmojiInfo(
            isOnlyEmojis = !hasNonEmojiContent,
            numEmojis = count
        )
    }

    fun replaceEmojis(spannable: Spannable?, spanSizePx: Float?) {
        if (spannable == null || spannable.isEmpty() || !EmojiManager.isInstalled()) return

        val typeface = EmojiManager.getTypeface()

        val existingSpans = spannable.getSpans(0, spannable.length, EmojiTypefaceSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        forEachGrapheme(spannable, EmojiManager.getEmojiIndex()) { _, start, end, isEmoji ->
            if (isEmoji) {
                spannable.setSpan(
                    EmojiTypefaceSpan(typeface, spanSizePx),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private inline fun forEachGrapheme(
        text: CharSequence,
        index: EmojiIndex,
        action: (grapheme: String, start: Int, end: Int, isEmoji: Boolean) -> Unit
    ) {
        val boundary = BreakIterator.getCharacterInstance()
        boundary.setText(text.toString())

        var start = boundary.first()
        var end = boundary.next()

        while (end != BreakIterator.DONE) {
            val grapheme = text.subSequence(start, end).toString()
            action(grapheme, start, end, index.contains(grapheme))
            start = end
            end = boundary.next()
        }
    }
}
