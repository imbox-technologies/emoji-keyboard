/*
 * Copyright 2026 - David Periañez and contributors
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

package com.davidperi.emojikeyboard.ui.view.components

import android.annotation.SuppressLint
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

@SuppressLint("ViewConstructor")
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
            setTextColor(context.getColor(R.color.emjkb_gray))
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