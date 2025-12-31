package com.davidperi.emojikeyboard.ui

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.ui.adapter.EmojiAdapter
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

internal class SearchResults(context: Context, private val delegate: EmojiDelegate) :
    FrameLayout(context) {

    private val recyclerView: RecyclerView
    private val emptyTextView: TextView
    private val adapter = EmojiAdapter { delegate.onEmojiClicked(it.unicode) }

    init {
        layoutParams = LayoutParams(MATCH_PARENT, 50.dp)

        adapter.isHorizontalLayout = true
        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = this@SearchResults.adapter
            setHasFixedSize(true)
        }

        emptyTextView = TextView(context).apply {
            text = "No emojis found"
            gravity = Gravity.CENTER
            setTextColor(context.getColor(R.color.emoji_keyboard_gray))
            isVisible = false
        }

        addView(recyclerView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        addView(emptyTextView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }


    // Public (internal) API
    fun setResults(emojis: List<EmojiListItem>) {
        if (emojis.isEmpty()) {
            emptyTextView.isVisible = true
            recyclerView.isVisible = false
        } else {
            emptyTextView.isVisible = false
            recyclerView.isVisible = true
            adapter.submitList(emojis)
            recyclerView.scrollToPosition(0)
        }
    }

}