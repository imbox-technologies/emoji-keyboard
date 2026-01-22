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

package com.davidperi.emojikeyboard.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.davidperi.emojikeyboard.utils.DisplayUtils.dp

class SpacerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): SpacerViewHolder {
            val view = View(parent.context).apply {
                visibility = View.INVISIBLE
                layoutParams = ViewGroup.LayoutParams(1, 1)
            }
            return SpacerViewHolder(view)
        }
    }

    fun bind(item: EmojiListItem.Spacer) {
        itemView.updateLayoutParams {
            if (!item.isFiller) {
                width = 40.dp
                height = ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                width = 1
                height = 1
            }
        }
    }
}