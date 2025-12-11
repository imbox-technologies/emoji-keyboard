package com.davidperi.emojikeyboard.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.davidperi.emojikeyboard.EmojiPopup
import com.davidperi.emojikeyboard.ui.adapter.EmojiAdapter
import com.davidperi.emojikeyboard.databinding.EmojiKeyboardPopupBinding
import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.provider.CustomEmojiProvider
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem

class EmojiKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = EmojiKeyboardPopupBinding.inflate(LayoutInflater.from(context), this, true)

    private var controller: EmojiPopup? = null
    private val adapter = EmojiAdapter { emoji ->
        Log.d("EMOJI", "emoji clicked: ${emoji.unicode}, ${emoji.description}")
    }

    init {
        setupAdapter()
        loadEmojis()
    }

    // PUBLIC API
    enum class PopupState { COLLAPSED, BEHIND, FOCUSED, SEARCHING }
    fun setupWith(editText: EditText) { controller = EmojiPopup(this, editText)
    }
    fun toggle() { controller?.toggle() }
    fun hide() { controller?.hide() }
    fun state() = controller?.state


    private fun setupAdapter() {
        val spanCount = 9
        val gridManager = GridLayoutManager(context, spanCount)

        gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val type = adapter.getItemViewType(position)
                return if (type == EmojiAdapter.Companion.VIEW_TYPE_EMOJI) 1 else spanCount
            }
        }

        binding.rvEmojis.apply {
            layoutManager = gridManager
            this.adapter = this@EmojiKeyboard.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadEmojis() {
        val data = CustomEmojiProvider.getCategories().toListItem()
        adapter.submitList(data)
    }

    private fun List<Category>.toListItem(): List<EmojiListItem> {
        return flatMap { category ->
            buildList {
                add(EmojiListItem.Header(category))
                category.emojis.forEach { emoji ->
                    add(EmojiListItem.EmojiKey(emoji))
                }
            }
        }
    }

    internal val searchBar = binding.searchBar.searchBar
    internal val topBar = binding.topBar
}