/*
 * Copyright 2026 - David Periañez and contributors
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

package com.imbox.emojikeyboard.ui.adapter

import com.imbox.emojikeyboard.data.model.Category
import com.imbox.emojikeyboard.data.model.Emoji

sealed class EmojiListItem {
    data class Header(val category: Category): EmojiListItem()
    data class EmojiKey(val emoji: Emoji): EmojiListItem()
    data class Spacer(val isFiller: Boolean) : EmojiListItem()
}