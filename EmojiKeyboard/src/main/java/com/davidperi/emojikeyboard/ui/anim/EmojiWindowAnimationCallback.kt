package com.davidperi.emojikeyboard.ui.anim

interface EmojiWindowAnimationCallback {
    fun onPrepare() {}
    fun onStart(targetHeight: Int) {}
    fun onProgress(fraction: Float)
    fun onEnd() {}
}