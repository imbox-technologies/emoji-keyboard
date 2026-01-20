package com.davidperi.emojikeyboardtest

import android.app.Application
import com.davidperi.emojikeyboard.EmojiConfig
import com.davidperi.emojikeyboard.EmojiLayoutMode
import com.davidperi.emojikeyboard.EmojiManager

class EmojiKeyboardTestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        EmojiManager.install(
            context = this,
            config = EmojiConfig(
                layoutMode = EmojiLayoutMode.COOPER
            )
        )
    }
}