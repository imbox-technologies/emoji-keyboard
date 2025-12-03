package com.davidperi.emojikeyboardtest.utils

import android.content.res.Resources

object MeasureUtils {

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

}