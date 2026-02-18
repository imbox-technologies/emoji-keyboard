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

package com.imbox.emojikeyboardtest

import android.app.Application
import android.graphics.Typeface
import com.imbox.emojikeyboard.EmojiConfig
import com.imbox.emojikeyboard.EmojiLayoutMode
import com.imbox.emojikeyboard.EmojiManager
import com.imbox.emojikeyboard.EmojiThemeMode
import com.imbox.emojikeyboardtest.prefs.KeyboardConfigPrefs

class EmojiKeyboardTestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val configPrefs = KeyboardConfigPrefs(this)
        val font = if (configPrefs.fontId == "cooper") {
            runCatching { Typeface.createFromAsset(assets, "fonts/Cooper.ttf") }.getOrNull()
        } else null
        val layoutMode = configPrefs.layoutMode
        val themeMode = configPrefs.themeMode
        EmojiManager.install(
            context = this,
            config = EmojiConfig(
                font = font,
                layoutMode = layoutMode,
                themeMode = themeMode
            )
        )
    }
}