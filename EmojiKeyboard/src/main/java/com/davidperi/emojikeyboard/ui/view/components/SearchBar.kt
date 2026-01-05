package com.davidperi.emojikeyboard.ui.view.components

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

internal class SearchBar(context: Context, private val delegate: EmojiDelegate) :
    CardView(context) {

    private val editText: EditText
    private val clearButton: AppCompatImageView


    init {
        // Styling
        radius = 8.dp.toFloat()
        cardElevation = 2.dp.toFloat()
        setCardBackgroundColor(context.getColor(R.color.emoji_keyboard_white))
        useCompatPadding = true

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
    }


    // Component builders
    private fun buildRootLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, 8.dp, 8.dp, 8.dp)
        }
    }

    private fun buildEditText(): EditText {
        return EditText(context).apply {
            hint = "Search emojis"
            background = null
            inputType = InputType.TYPE_CLASS_TEXT
            maxLines = 1
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_NONE
            setTextColor(context.getColor(R.color.emoji_keyboard_black))
            setHintTextColor(context.getColor(R.color.emoji_keyboard_gray))
            textSize = 16f
        }
    }

    private fun buildClearButton(): AppCompatImageView {
        return AppCompatImageView(context).apply {
            setImageResource(R.drawable.cross)
            visibility = GONE
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                marginEnd = 2.dp
            }
            setOnClickListener { editText.text.clear() }
        }
    }

    private fun buildSearchIcon(): AppCompatImageView {
        return AppCompatImageView(context).apply {
            setImageResource(R.drawable.search)
            imageTintList = context.getColorStateList(R.color.emoji_keyboard_gray)
            layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp).apply {
                marginStart = 4.dp
                marginEnd = 8.dp
            }
        }
    }

}