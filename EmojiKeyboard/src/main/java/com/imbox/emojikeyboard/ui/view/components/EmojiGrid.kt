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

package com.imbox.emojikeyboard.ui.view.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imbox.emojikeyboard.ui.view.EmojiDelegate
import com.imbox.emojikeyboard.ui.adapter.EmojiAdapter
import com.imbox.emojikeyboard.ui.adapter.EmojiListItem
import com.imbox.emojikeyboard.utils.DisplayUtils.px

@SuppressLint("ViewConstructor")
internal class EmojiGrid(context: Context, private val delegate: EmojiDelegate) :
    RecyclerView(context) {

    private val emojisAdapter = EmojiAdapter { delegate.onEmojiClicked(it.unicode) }
    private val recentAdapter = EmojiAdapter { delegate.onEmojiClicked(it.unicode) }
    private val concatAdapter = ConcatAdapter(
        ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build(),
        recentAdapter,
        emojisAdapter
    )

    private var isProgrammaticScroll = false
    private var currentSpanCount = 0
    private var isHorizontalMode = false

    var onSpanCountChanged: ((Int) -> Unit)? = null


    init {
        id = generateViewId()
        adapter = concatAdapter
        itemAnimator = null
        setHasFixedSize(true)

        setupScrollListener()
    }


    // Public (internal) API
    fun setup(spanCount: Int, orientation: Int) {
        isHorizontalMode = orientation == HORIZONTAL
        currentSpanCount = spanCount

        val gridManager = GridLayoutManager(context, spanCount, orientation, false)

        gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (orientation == VERTICAL) {
                    val type = adapter?.getItemViewType(position)
                    return if (type == EmojiAdapter.Companion.VIEW_TYPE_EMOJI) 1 else spanCount
                } else {
                    return 1
                }
            }
        }

        layoutManager = gridManager
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (!isHorizontalMode || h <= 0) return

        val newSpan = calculateSpanForHeight(h)
        if (newSpan != currentSpanCount) {
            currentSpanCount = newSpan
            onSpanCountChanged?.invoke(newSpan)
        }
    }

    private fun calculateSpanForHeight(heightPx: Int): Int {
        val heightDp = heightPx.px
        return when {
            heightDp < 200 -> 3
            heightDp < 240 -> 4
            heightDp < 280 -> 5
            else -> 6
        }
    }

    fun scrollToPosition(position: Int, offset: Int = 0) {
        isProgrammaticScroll = true
        val lm = layoutManager as GridLayoutManager
        lm.scrollToPositionWithOffset(position, offset)

        post {
            if (scrollState == SCROLL_STATE_IDLE) {
                isProgrammaticScroll = false
            }
        }
    }

    fun submitEmojis(items: List<EmojiListItem>) = emojisAdapter.submitList(items)

    fun submitRecent(items: List<EmojiListItem>) = recentAdapter.submitList(items)


    private fun setupScrollListener() {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isProgrammaticScroll) return

                val lm = layoutManager as GridLayoutManager
                val firstPos = lm.findFirstVisibleItemPosition()

                if (firstPos != NO_POSITION) {
                    delegate.onGridScrolled(firstPos)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE) {
                    isProgrammaticScroll = false
                }
            }
        })
    }

}