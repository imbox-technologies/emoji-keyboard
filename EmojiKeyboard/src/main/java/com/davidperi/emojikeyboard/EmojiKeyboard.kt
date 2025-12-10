package com.davidperi.emojikeyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.davidperi.emojikeyboard.adapter.EmojiAdapter
import com.davidperi.emojikeyboard.databinding.EmojiKeyboardPopupBinding

class EmojiKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = EmojiKeyboardPopupBinding.inflate(LayoutInflater.from(context), this, true)

    private var controller: EmojiPopup? = null
    private val adapter = EmojiAdapter { }
    private val provider = EmojiProvider()

    init {
        setupAdapter()
        loadEmojis()
    }

    // PUBLIC API
    enum class PopupState { COLLAPSED, BEHIND, FOCUSED, SEARCHING }
    fun setupWith(editText: EditText) { controller = EmojiPopup(this, editText) }
    fun toggle() { controller?.toggle() }
    fun hide() { controller?.hide() }
    fun state() = controller?.state


    private fun setupAdapter() {
        val spanCount = 9
        val gridManager = GridLayoutManager(context, spanCount)

        gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val type = adapter.getItemViewType(position)
                return if (type == EmojiAdapter.VIEW_TYPE_EMOJI) 1 else spanCount
            }
        }

        binding.rvEmojis.apply {
            layoutManager = gridManager
            this.adapter = this@EmojiKeyboard.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadEmojis() {
        val data = provider.getEmojis()
        adapter.submitList(data)
    }

    internal val searchBar = binding.searchBar.searchBar
    internal val topBar = binding.topBar
}