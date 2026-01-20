package com.davidperi.emojikeyboard.utils

import android.text.Spannable
import com.davidperi.emojikeyboard.EmojiManager
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
        val index = EmojiManager.getEmojiIndex()
        val boundary = BreakIterator.getCharacterInstance()
        boundary.setText(text.toString())

        var start = boundary.first()
        var end = boundary.next()

        while (end != BreakIterator.DONE) {
            val chunk = text.subSequence(start, end).toString()
            if (index.contains(chunk)) {
                result.add(start until end)
            }
            start = end
            end = boundary.next()
        }

        return result
    }

    fun CharSequence?.getEmojiInfo(): EmojiInfo {
        if (this.isNullOrEmpty() || !EmojiManager.isInstalled()) {
            return EmojiInfo(isOnlyEmojis = false, numEmojis = 0)
        }

        val index = EmojiManager.getEmojiIndex()
        val boundary = BreakIterator.getCharacterInstance()
        boundary.setText(this.toString())

        var count = 0
        var hasNonEmojiContent = false

        var start = boundary.first()
        var end = boundary.next()

        while (end != BreakIterator.DONE) {
            val chunk = this.subSequence(start, end).toString()
            if (index.contains(chunk)) {
                count++
            } else if (chunk.isNotBlank()) {
                hasNonEmojiContent = true
            }
            start = end
            end = boundary.next()
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
        val index = EmojiManager.getEmojiIndex()

        val existingSpans = spannable.getSpans(0, spannable.length, EmojiTypefaceSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val boundary = BreakIterator.getCharacterInstance()
        boundary.setText(spannable.toString())

        var start = boundary.first()
        var end = boundary.next()

        while (end != BreakIterator.DONE) {
            val chunk = spannable.subSequence(start, end).toString()
            if (index.contains(chunk)) {
                spannable.setSpan(
                    EmojiTypefaceSpan(typeface, spanSizePx),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            start = end
            end = boundary.next()
        }
    }
}
