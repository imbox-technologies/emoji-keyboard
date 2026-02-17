/*
 * Copyright 2026 - IMBox Technologies and contributors
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

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.imbox.emojikeyboard.R
import com.imbox.emojikeyboard.utils.DisplayUtils.dp
import com.imbox.emojikeyboard.utils.EmojiThemeHelper.resolveColor

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
        setTextSize(14f)
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(resolveColor(context, R.attr.emjkb_colorOnBackground))
    }

}