package com.davidperi.emojikeyboard.ui

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.davidperi.emojikeyboard.ui.adapter.EmojiAdapter
import com.davidperi.emojikeyboard.databinding.EmojiKeyboardPopupBinding
import com.davidperi.emojikeyboard.provider.DefaultEmojiProvider
import com.davidperi.emojikeyboard.ui.adapter.EmojiListMapper
import com.davidperi.emojikeyboard.ui.span.EmojiTypefaceSpan
import com.davidperi.emojikeyboard.utils.EmojiFontManager
import kotlin.math.max
import kotlin.math.min

class EmojiKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = EmojiKeyboardPopupBinding.inflate(LayoutInflater.from(context), this, true)

    private var controller: PopupStateMachine? = null
    private val adapter = EmojiAdapter { emoji ->
        Log.d("EMOJI", "emoji clicked: ${emoji.unicode}, ${emoji.description}")
        onEmojiSelected(emoji.unicode)
    }
    private val provider = DefaultEmojiProvider  // Later this will be selected by the user
    private var targetEditText: EditText? = null

    init {
        setupAdapter()
        loadEmojis()
    }

    // PUBLIC API
    enum class PopupState { COLLAPSED, BEHIND, FOCUSED, SEARCHING }

    fun setupWith(editText: EditText) {
        this.targetEditText = editText
        controller = PopupStateMachine(this, editText)
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
            this.adapter = this@EmojiKeyboardView.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadEmojis() {
        val data = EmojiListMapper.map(provider.getCategories())
        adapter.submitList(data)
    }


    private fun onEmojiSelected(unicode: String) {
        val editText = targetEditText ?: return

        val start = editText.selectionStart.coerceAtLeast(0)
        val end = editText.selectionEnd.coerceAtLeast(0)

        val spannableString = SpannableString(unicode)
        val typeface = EmojiFontManager.getTypeface(context)

        spannableString.setSpan(
            EmojiTypefaceSpan(typeface),
            0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        editText.text.replace(
            min(start, end),
            max(start, end),
            spannableString
        )
    }


    internal val searchBar = binding.searchBar.searchBar
    internal val topBar = binding.topBar


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        targetEditText = null
    }
}
