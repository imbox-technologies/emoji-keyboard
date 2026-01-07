package com.davidperi.emojikeyboard.ui.view.components

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

class EmojiHeaderItem (context: Context) : AppCompatTextView(context) {

    init {
        layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        val start = 12.dp
        val top = 12.dp
        val end = 4.dp
        val bottom = 0
        setPaddingRelative(start, top, end, bottom)

        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setSingleLine()

        setTextAppearance(context, R.style.HeaderText)
    }

}