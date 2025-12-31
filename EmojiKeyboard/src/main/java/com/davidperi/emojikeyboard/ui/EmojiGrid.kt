package com.davidperi.emojikeyboard.ui

import android.content.Context
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.ui.adapter.EmojiAdapter
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem

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


    init {
        id = generateViewId()
        adapter = concatAdapter
        itemAnimator = null
        setHasFixedSize(true)

        setupScrollListener()
    }


    // Public (internal) API
    fun setup(spanCount: Int, orientation: Int) {
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