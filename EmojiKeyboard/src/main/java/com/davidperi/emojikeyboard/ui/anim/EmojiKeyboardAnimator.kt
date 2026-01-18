package com.davidperi.emojikeyboard.ui.anim

import android.util.Log
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.davidperi.emojikeyboard.EmojiPopup
import com.davidperi.emojikeyboard.utils.lerp
import kotlin.math.max

fun ViewGroup.setupEmojiPopupAnimation(emojiPopup: EmojiPopup) {
    val callback = object : EmojiWindowAnimationCallback {
        var startBottom = 0
        var endBottom = 0

        override fun onPrepare() {
            startBottom = paddingBottom
            Log.e("EMOJI PopupAnimation", "popup onPrepare (start=$startBottom)")
        }

        override fun onStart(targetHeight: Int) {
            val minBottom = ViewCompat.getRootWindowInsets(this@setupEmojiPopupAnimation)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0
            endBottom = max(targetHeight, minBottom)
            Log.e("EMOJI PopupAnimation", "popup onStart (end=$endBottom)")
        }

        override fun onProgress(fraction: Float) {
            val offset = lerp(startBottom, endBottom, fraction)
            updatePadding(bottom = offset.toInt())
        }

        override fun onEnd() {
            Log.e("EMOJI PopupAnimation", "popup onEnd ")
            ViewCompat.requestApplyInsets(rootView)
        }
    }

    emojiPopup.setAnimationCallback(callback)
}