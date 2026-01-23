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

package com.imbox.emojikeyboard.ui.span

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class EmojiTypefaceSpan(
    private val typeface: Typeface,
    private val sizePx: Float? = null
) : MetricAffectingSpan() {

    override fun updateMeasureState(p0: TextPaint) {
        apply(p0)
    }

    override fun updateDrawState(p0: TextPaint) {
        apply(p0)
    }

    private fun apply(paint: Paint) {
//        val old = paint.typeface
//        val oldStyle = old?.style ?: 0

        paint.typeface = typeface

        if (sizePx != null) {
            paint.textSize = sizePx
        }
    }
}