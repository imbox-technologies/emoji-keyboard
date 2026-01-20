package com.davidperi.emojikeyboard.ui.view.components

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.ui.view.EmojiDelegate
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

@SuppressLint("ViewConstructor")
internal class SearchBar(context: Context, private val delegate: EmojiDelegate) :
    CardView(context) {

    private val editText: EditText
    private val clearButton: AppCompatImageView

    var onFocus: (() -> Unit)? = null
    var onFocusLost: (() -> Unit)? = null

    init {
        // Styling
        radius = 20.dp.toFloat()
        cardElevation = 0f // 2.dp.toFloat()
        setCardBackgroundColor(context.getColor(R.color.emjkb_white))

        preventCornerOverlap = false
        useCompatPadding = false

        // Components
        val rootLayout = buildRootLayout()
        val searchIcon = buildSearchIcon()
        editText = buildEditText()
        clearButton = buildClearButton()

        // Layout
        rootLayout.addView(searchIcon)
        rootLayout.addView(editText, LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        rootLayout.addView(clearButton)
        addView(rootLayout)

        // Logic
        editText.addTextChangedListener {
            val query = it?.toString() ?: ""
            clearButton.isVisible = query.isNotEmpty()
            delegate.onQueryChanged(query)
        }

        editText.setOnFocusChangeListener { _, hasFocus ->
            delegate.onSearchFocusChange(hasFocus)
        }
    }


    // Public (internal) API
    fun isInputFocused(): Boolean {
        return editText.hasFocus()
    }

    fun clearInput() {
        editText.text.clear()
    }


    // Component builders
    private fun buildRootLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, 0, 8.dp, 0)
        }
    }

    private fun buildEditText(): EditText {
        return EditText(context).apply {
            hint = context.getString(R.string.search_bar_edit_text_hint)
            background = null
            inputType = InputType.TYPE_CLASS_TEXT
            maxLines = 1
            isSingleLine = true
            includeFontPadding = false
            imeOptions = EditorInfo.IME_ACTION_NONE
            setTextColor(context.getColor(R.color.emjkb_black))
            setHintTextColor(context.getColor(R.color.emjkb_gray))
            textSize = 16f
        }
    }

    private fun buildClearButton(): AppCompatImageView {
        return AppCompatImageView(context).apply {
            setImageResource(R.drawable.emjkb_cross)
            visibility = GONE
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                marginEnd = 2.dp
            }
            setOnClickListener { editText.text.clear() }
        }
    }

    private fun buildSearchIcon(): AppCompatImageView {
        return AppCompatImageView(context).apply {
            setImageResource(R.drawable.emjkb_search)
            imageTintList = context.getColorStateList(R.color.emjkb_gray)
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                marginStart = 4.dp
                marginEnd = 8.dp
            }
        }
    }

}