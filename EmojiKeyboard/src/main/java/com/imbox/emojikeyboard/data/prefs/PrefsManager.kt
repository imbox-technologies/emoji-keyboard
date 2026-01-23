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

package com.imbox.emojikeyboard.data.prefs

import android.content.Context
import androidx.core.content.edit

class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    var lastKeyboardHeight: Int
        get() = prefs.getInt(KEY_HEIGHT, -1)
        set(value) = prefs.edit { putInt(KEY_HEIGHT, value) }

    companion object {
        const val PREFS_KEY = "emoji_keyboard_prefs"
        const val DEFAULT_HEIGHT_DP = 300
        private const val KEY_HEIGHT = "emoji_keyboard_prefs_height"
    }
}