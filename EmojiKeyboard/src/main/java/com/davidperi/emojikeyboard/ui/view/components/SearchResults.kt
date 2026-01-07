package com.davidperi.emojikeyboard.ui.view.components

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.ui.view.EmojiDelegate
import com.davidperi.emojikeyboard.ui.adapter.EmojiAdapter
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

internal class SearchResults(context: Context, private val delegate: EmojiDelegate) :
    FrameLayout(context) {

    private val recyclerView: RecyclerView
    private val emptyTextView: TextView
    private val adapter = EmojiAdapter { delegate.onEmojiClicked(it.unicode) }

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 50.dp)

        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = this@SearchResults.adapter
            setHasFixedSize(true)
        }

        emptyTextView = TextView(context).apply {
            text = context.getString(R.string.search_results_text_no_emojis_found)
            gravity = Gravity.CENTER
            setTextColor(context.getColor(R.color.emoji_keyboard_gray))
            isVisible = false
        }

        addView(recyclerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(emptyTextView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }


    // Public (internal) API
    fun setResults(emojis: List<EmojiListItem>) {
        if (emojis.isEmpty()) {
            emptyTextView.isVisible = true
            recyclerView.isVisible = false
        } else {
            emptyTextView.isVisible = false
            recyclerView.isVisible = true
            adapter.submitList(emojis) {
                recyclerView.scrollToPosition(0)
            }
        }
    }

}