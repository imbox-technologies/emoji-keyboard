package com.davidperi.emojikeyboard.provider

import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.ui.adapter.EmojiListItem
import kotlin.collections.plus

//internal class EmojiProvider {
//    fun getEmojis(): List<EmojiListItem> {
//        val random_emojis = listOf(
//            "🧿","🪬","🪩","🧸","🪵","🪨","🪶","🪴","🛟",
//            "🪝","🧲","🪜","🧯","🪪","🪫","🛜","🧃","🫙",
//            "🫛","🪺","🫧","🫳","🫴","🧍","🧎","🧑‍🚀","🧑‍🍳"
//        )
//
//        val smileys_and_people = EmojiListItem.Header("Smileys & People")
//        val people_emojis = listOf(
//            "😀","😃","😄","😁","😆","😊","🙂","🙃","😉",
//            "😌","😍","😘","😗","😙","😚","🥰","😇","🤩",
//            "🥳","😎","🤓","🧐","🤯","🤠","🥸","🤗","🤮"
//        )
//
//        val animals_and_nature = EmojiListItem.Header("Animals & Nature")
//        val animal_emojis = listOf(
//            "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🦁",
//            "🐯","🐨","🐸","🐵","🐔","🐧","🐦","🐤","🦆",
//            "🦅","🦉","🦇","🐺","🦄","🐝","🐛","🦋","🦑"
//        )
//
//        val food_and_drink = EmojiListItem.Header("Food & Drink")
//        val food_emojis = listOf(
//            "🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓",
//            "🫐","🍒","🍑","🥭","🍍","🥝","🍅","🥑","🍆",
//            "🥕","🌽","🍞","🧀","🍔","🍕","🍟","🌮","🍣"
//        )
//
//        val activities = EmojiListItem.Header("Activities")
//        val activity_emojis = listOf(
//            "⚽","🏀","🏈","⚾","🎾","🏐","🏉","🥏","🎱",
//            "🏓","🏸","🥊","🥋","⛳","🪁","🏹","🛷","⛷️",
//            "🏂","🏄‍♂️","🏊‍♀️","🤽‍♂️","🚴‍♀️","🚵‍♂️","🧗‍♀️","🤺","🎿"
//        )
//
//        val travel_and_places = EmojiListItem.Header("Travel & Places")
//        val travel_emojis = listOf(
//            "🚗","🚕","🚙","🚌","🚎","🏎️","🚓","🚑","🚒",
//            "🚐","🚚","🚛","🚜","✈️","🛩️","🚀","🛸","🚁",
//            "🛳️","⛴️","🚤","🗿","🗼","🗽","🗺️","🏖️","🏔️"
//        )
//
//        val objects = EmojiListItem.Header("Objects")
//        val object_emojis = listOf(
//            "💻","🖥️","🖨️","⌨️","🖱️","🖲️","📱","📲","📞",
//            "📡","🛰️","🔋","🔌","💾","💿","📀","🔧","🔨",
//            "🛠️","🧰","🔬","🔭","🔍","🤖","🧠","🧪","📡"
//        )
//
//        val symbols = EmojiListItem.Header("Symbols")
//        val symbol_emojis = listOf(
//            "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎",
//            "💔","❣️","💕","💞","☮️","✝️","☪️","🕉️","☸️",
//            "✡️","🔯","☯️","♻️","⚠️","✅","❌","❓","❗"
//        )
//
//        val flags = EmojiListItem.Header("Flags")
//        val flag_emojis = listOf(
//            "🇺🇸","🇬🇧","🇪🇸","🇫🇷","🇩🇪","🇮🇹","🇵🇹","🇳🇱","🇧🇪",
//            "🇨🇦","🇲🇽","🇧🇷","🇦🇷","🇨🇱","🇨🇴","🇵🇪","🇦🇺","🇳🇿",
//            "🇯🇵","🇨🇳","🇰🇷","🇮🇳","🇸🇦","🇹🇷","🇷🇺","🇿🇦","🇪🇬"
//        )
//
//        return convertList(random_emojis) +
//                listOf(smileys_and_people) + convertList(people_emojis) +
//                listOf(animals_and_nature) + convertList(animal_emojis) +
//                listOf(food_and_drink) + convertList(food_emojis) +
//                listOf(activities) + convertList(activity_emojis) +
//                listOf(travel_and_places) + convertList(travel_emojis) +
//                listOf(objects) + convertList(object_emojis) +
//                listOf(symbols) + convertList(symbol_emojis) +
//                listOf(flags) + convertList(flag_emojis)
//    }
//
//    private fun convertList(list: List<String>): List<EmojiListItem.EmojiKey> {
//        return list.map { EmojiListItem.EmojiKey(it) }
//    }
//}

interface EmojiProvider {
    fun getCategories(): List<Category>
}