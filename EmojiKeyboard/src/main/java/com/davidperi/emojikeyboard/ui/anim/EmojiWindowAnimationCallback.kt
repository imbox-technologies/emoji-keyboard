package com.davidperi.emojikeyboard.ui.anim

interface EmojiWindowAnimationCallback {
    fun onPrepare() {}
    fun onStart() {}
    fun onProgress(fraction: Float)
    fun onEnd() {}
}