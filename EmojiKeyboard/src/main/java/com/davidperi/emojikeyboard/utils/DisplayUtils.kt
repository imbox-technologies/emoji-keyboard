package com.davidperi.emojikeyboard.utils

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