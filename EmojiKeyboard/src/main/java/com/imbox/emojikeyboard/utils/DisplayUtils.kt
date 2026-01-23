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

package com.imbox.emojikeyboard.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import java.text.Normalizer

object DisplayUtils {

    fun ViewGroup.inflate(viewResource: Int): View {
        val inflater = LayoutInflater.from(this.context)
        return inflater.inflate(viewResource, this, false)
    }

    fun LinearLayout.inflate(viewResource: Int): View {
        val inflater = LayoutInflater.from(this.context)
        return inflater.inflate(viewResource, this, false)
    }

    fun View.hideKeyboard() {
        val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }

    fun View.showKeyboard() {
        this.requestFocus()
        val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    val Int.px: Int
        get() = (this / Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    fun String.removeAccents(): String {
        val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return regex.replace(temp, "")
    }

    fun View.debugBorder(color: Int = Color.RED) {
        val border = GradientDrawable()
        border.setStroke(4, color)
        this.background = border
    }

}