package com.davidperi.emojikeyboard.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

object BindingUtils {

    fun ViewGroup.inflate(viewResource: Int): View {
        val inflater = LayoutInflater.from(this.context)
        return inflater.inflate(viewResource, this, false)
    }

}
