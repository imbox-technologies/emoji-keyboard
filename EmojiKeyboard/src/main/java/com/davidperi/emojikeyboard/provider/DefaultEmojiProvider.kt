package com.davidperi.emojikeyboard.provider

import android.content.Context
import com.davidperi.emojikeyboard.R
import com.davidperi.emojikeyboard.model.Category
import com.davidperi.emojikeyboard.model.Emoji

object DefaultEmojiProvider : EmojiProvider {
    private val faces by lazy {
        listOf(
            Emoji("\uD83D\uDE00", "Grinning Face"),
            Emoji("\uD83D\uDE01", "Beaming Face with Smiling Eyes"),
            Emoji("\uD83D\uDE02", "Face with Tears of Joy"),
            Emoji("\uD83E\uDD23", "Rolling on the Floor Laughing"),
            Emoji("\uD83D\uDE03", "Grinning Face with Big Eyes"),
            Emoji("\uD83D\uDE04", "Grinning Face with Smiling Eyes"),
            Emoji("\uD83D\uDE05", "Grinning Face with Sweat"),
            Emoji("\uD83D\uDE06", "Grinning Squinting Face"),
            Emoji("\uD83D\uDE09", "Winking Face"),
            Emoji("\uD83D\uDE0A", "Smiling Face with Smiling Eyes"),
            Emoji("\uD83D\uDE0B", "Face Savoring Food"),
            Emoji("\uD83D\uDE0E", "Smiling Face with Sunglasses"),
            Emoji("\uD83D\uDE0D", "Smiling Face with Heart-Eyes"),
            Emoji("\uD83D\uDE18", "Face Blowing a Kiss"),
            Emoji("\uD83D\uDE17", "Kissing Face"),
            Emoji("\uD83D\uDE19", "Kissing Face with Smiling Eyes"),
            Emoji("\uD83D\uDE1A", "Kissing Face with Closed Eyes"),
            Emoji("\uD83D\uDE42", "Slightly Smiling Face"),
            Emoji("\uD83E\uDD17", "Hugging Face"),
            Emoji("\uD83E\uDD29", "Star-Struck"),
            Emoji("\uD83E\uDD14", "Thinking Face"),
            Emoji("\uD83E\uDD28", "Face with Raised Eyebrow"),
            Emoji("\uD83D\uDE10", "Neutral Face"),
            Emoji("\uD83D\uDE11", "Expressionless Face"),
            Emoji("\uD83D\uDE36", "Face Without Mouth"),
            Emoji("\uD83D\uDE44", "Face with Rolling Eyes"),
            Emoji("\uD83D\uDE0F", "Smirking Face")
        )
    }
    private val nature by lazy {
        listOf(
            Emoji("\uD83D\uDC36", "Dog Face"),
            Emoji("\uD83D\uDC31", "Cat Face"),
            Emoji("\uD83D\uDC2D", "Mouse Face"),
            Emoji("\uD83D\uDC39", "Hamster Face"),
            Emoji("\uD83D\uDC30", "Rabbit Face"),
            Emoji("\uD83E\uDD8A", "Fox Face"),
            Emoji("\uD83D\uDC3B", "Bear Face"),
            Emoji("\uD83D\uDC3C", "Panda"),
            Emoji("\uD83D\uDC28", "Koala"),
            Emoji("\uD83D\uDC2F", "Tiger Face"),
            Emoji("\uD83E\uDD81", "Lion"),
            Emoji("\uD83D\uDC2E", "Cow Face"),
            Emoji("\uD83D\uDC37", "Pig Face"),
            Emoji("\uD83D\uDC38", "Frog"),
            Emoji("\uD83D\uDC35", "Monkey Face"),
            Emoji("\uD83D\uDC14", "Chicken"),
            Emoji("\uD83D\uDC27", "Penguin"),
            Emoji("\uD83D\uDC26", "Bird"),
            Emoji("\uD83D\uDC24", "Baby Chick"),
            Emoji("\uD83D\uDC3A", "Wolf"),
            Emoji("\uD83E\uDD84", "Unicorn"),
            Emoji("\uD83D\uDC1D", "Bee"),
            Emoji("\uD83D\uDC1B", "Bug"),
            Emoji("\uD83E\uDD8B", "Butterfly"),
            Emoji("\uD83D\uDC0C", "Snail"),
            Emoji("\uD83D\uDC1E", "Lady Beetle"),
            Emoji("\uD83C\uDF38", "Cherry Blossom")
        )
    }
    private val food by lazy {
        listOf(
            Emoji("\uD83C\uDF4F", "Green Apple"),
            Emoji("\uD83C\uDF4E", "Red Apple"),
            Emoji("\uD83C\uDF50", "Pear"),
            Emoji("\uD83C\uDF4A", "Tangerine"),
            Emoji("\uD83C\uDF4B", "Lemon"),
            Emoji("\uD83C\uDF4C", "Banana"),
            Emoji("\uD83C\uDF49", "Watermelon"),
            Emoji("\uD83C\uDF47", "Grapes"),
            Emoji("\uD83C\uDF53", "Strawberry"),
            Emoji("\uD83E\uDD66", "Blueberries"),
            Emoji("\uD83C\uDF48", "Melon"),
            Emoji("\uD83C\uDF52", "Cherries"),
            Emoji("\uD83C\uDF51", "Peach"),
            Emoji("\uD83E\uDD6D", "Mango"),
            Emoji("\uD83C\uDF4D", "Pineapple"),
            Emoji("\uD83E\uDD65", "Coconut"),
            Emoji("\uD83E\uDD5D", "Kiwi Fruit"),
            Emoji("\uD83C\uDF45", "Tomato"),
            Emoji("\uD83E\uDD51", "Avocado"),
            Emoji("\uD83C\uDF5E", "Bread"),
            Emoji("\uD83E\uDD50", "Croissant"),
            Emoji("\uD83E\uDD68", "Pretzel"),
            Emoji("\uD83C\uDF5F", "French Fries"),
            Emoji("\uD83C\uDF55", "Pizza"),
            Emoji("\uD83C\uDF2D", "Hot Dog"),
            Emoji("\uD83C\uDF54", "Hamburger"),
            Emoji("\uD83C\uDF63", "Sushi")
        )
    }
    private val activity by lazy {
        listOf(
            Emoji("\u26BD", "Soccer Ball"),
            Emoji("\uD83C\uDFC0", "Basketball"),
            Emoji("\uD83C\uDFC8", "American Football"),
            Emoji("\u26BE", "Baseball"),
            Emoji("\uD83C\uDFBE", "Tennis"),
            Emoji("\uD83C\uDFD0", "Volleyball"),
            Emoji("\uD83C\uDFC9", "Rugby Football"),
            Emoji("\uD83C\uDFB1", "Pool 8 Ball"),
            Emoji("\uD83C\uDFD3", "Ping Pong"),
            Emoji("\uD83C\uDFF8", "Badminton"),
            Emoji("\uD83E\uDD4B", "Goal Net"),
            Emoji("\uD83C\uDFB3", "Bowling"),
            Emoji("\uD83E\uDD4A", "Boxing Glove"),
            Emoji("\uD83E\uDD4B", "Martial Arts Uniform"),
            Emoji("\u26F3", "Flag in Hole"),
            Emoji("\uD83C\uDFF9", "Bow and Arrow"),
            Emoji("\uD83C\uDFA3", "Fishing Pole"),
            Emoji("\uD83E\uDE9F", "Diving Mask"),
            Emoji("\uD83C\uDFBD", "Running Shirt"),
            Emoji("\uD83D\uDEB4\u200D\u2642\uFE0F", "Man Biking"),
            Emoji("\uD83D\uDCAA\u200D\u2640\uFE0F", "Woman Lifting Weights"),
            Emoji("\uD83E\uDD38\u200D\u2642\uFE0F", "Man Cartwheeling"),
            Emoji("\u26F7\uFE0F", "Skier"),
            Emoji("\uD83C\uDFC2", "Snowboarder"),
            Emoji("\uD83D\uDEF8", "Roller Skate"),
            Emoji("\uD83D\uDEF9", "Skateboard"),
            Emoji("\uD83E\uDDB7\u200D\u2640\uFE0F", "Woman Climbing")
        )
    }
    private val travel by lazy {
        listOf(
            Emoji("\uD83D\uDE97", "Automobile"),
            Emoji("\uD83D\uDE95", "Taxi"),
            Emoji("\uD83D\uDE99", "SUV"),
            Emoji("\uD83D\uDE8C", "Bus"),
            Emoji("\uD83D\uDE8E", "Trolleybus"),
            Emoji("\uD83C\uDFCE\uFE0F", "Racing Car"),
            Emoji("\uD83D\uDE93", "Police Car"),
            Emoji("\uD83D\uDE91", "Ambulance"),
            Emoji("\uD83D\uDE92", "Fire Engine"),
            Emoji("\uD83D\uDE90", "Minivan"),
            Emoji("\u2708\uFE0F", "Airplane"),
            Emoji("\uD83D\uDE80", "Rocket"),
            Emoji("\uD83D\uDE81", "Helicopter"),
            Emoji("\u26F5", "Sailboat"),
            Emoji("\uD83D\uDEA2", "Ship"),
            Emoji("\uD83D\uDEA4", "Speedboat"),
            Emoji("\uD83D\uDEF3\uFE0F", "Passenger Ship"),
            Emoji("\uD83D\uDE89", "Station"),
            Emoji("\uD83D\uDE86", "Train"),
            Emoji("\uD83D\uDE84", "High-Speed Train"),
            Emoji("\uD83D\uDE87", "Metro"),
            Emoji("\uD83D\uDDFD", "Statue of Liberty"),
            Emoji("\uD83D\uDDFC", "Tokyo Tower"),
            Emoji("\uD83C\uDFF0", "Castle"),
            Emoji("\uD83C\uDFEF", "Japanese Castle"),
            Emoji("\uD83C\uDF0B", "Volcano"),
            Emoji("\uD83C\uDFDD\uFE0F", "Desert Island")
        )
    }
    private val objects by lazy {
        listOf(
            Emoji("\uD83D\uDCA1", "Light Bulb"),
            Emoji("\uD83D\uDD26", "Flashlight"),
            Emoji("\uD83D\uDD6F\uFE0F", "Candle"),
            Emoji("\uD83D\uDCF1", "Mobile Phone"),
            Emoji("\uD83D\uDCBB", "Laptop"),
            Emoji("\u231A", "Watch"),
            Emoji("\uD83D\uDCFA", "Television"),
            Emoji("\uD83D\uDCFB", "Radio"),
            Emoji("\uD83C\uDFA7", "Headphone"),
            Emoji("\uD83D\uDCF7", "Camera"),
            Emoji("\uD83D\uDCF8", "Camera with Flash"),
            Emoji("\uD83D\uDD79\uFE0F", "Joystick"),
            Emoji("\uD83D\uDCBD", "Computer Disk"),
            Emoji("\uD83D\uDCBE", "Floppy Disk"),
            Emoji("\uD83D\uDCBF", "Optical Disc"),
            Emoji("\uD83D\uDCC0", "DVD"),
            Emoji("\uD83C\uDF99\uFE0F", "Studio Microphone"),
            Emoji("\uD83C\uDF9A\uFE0F", "Level Slider"),
            Emoji("\uD83D\uDD0C", "Electric Plug"),
            Emoji("\uD83D\uDD0B", "Battery"),
            Emoji("\uD83D\uDEE0\uFE0F", "Hammer and Wrench"),
            Emoji("\uD83D\uDD27", "Wrench"),
            Emoji("\uD83D\uDD28", "Hammer"),
            Emoji("\uD83D\uDD29", "Nut and Bolt"),
            Emoji("\uD83D\uDCE6", "Package"),
            Emoji("\uD83D\uDECF\uFE0F", "Bed"),
            Emoji("\uD83E\uDE91", "Chair")
        )
    }
    private val symbols by lazy {
        listOf(
            Emoji("\u2764\uFE0F", "Red Heart"),
            Emoji("\uD83D\uDC9B", "Yellow Heart"),
            Emoji("\uD83D\uDC9A", "Green Heart"),
            Emoji("\uD83D\uDC99", "Blue Heart"),
            Emoji("\uD83D\uDC9C", "Purple Heart"),
            Emoji("\uD83E\uDD0E", "Orange Heart"),
            Emoji("\uD83D\uDDA4", "Black Heart"),
            Emoji("\uD83E\uDD0D", "White Heart"),
            Emoji("\uD83E\uDD0E", "Brown Heart"),
            Emoji("\u2757", "Exclamation Mark"),
            Emoji("\u2753", "Question Mark"),
            Emoji("\u203C\uFE0F", "Double Exclamation"),
            Emoji("\u2728", "Sparkles"),
            Emoji("\u2B50", "Star"),
            Emoji("\uD83C\uDF1F", "Glowing Star"),
            Emoji("\uD83D\uDCAB", "Dizzy Symbol"),
            Emoji("\uD83D\uDD25", "Fire"),
            Emoji("\uD83D\uDCAF", "Hundred Points"),
            Emoji("\u26A1", "High Voltage"),
            Emoji("\uD83D\uDCA2", "Anger Symbol"),
            Emoji("\uD83D\uDCA4", "Zzz"),
            Emoji("\uD83D\uDD14", "Bell"),
            Emoji("\uD83D\uDD15", "Bell with Slash"),
            Emoji("\uD83D\uDD12", "Locked"),
            Emoji("\uD83D\uDD13", "Unlocked"),
            Emoji("\uD83D\uDD11", "Key"),
            Emoji("\uD83D\uDED1", "Stop Sign")
        )
    }
    private val flags by lazy {
        listOf(
            Emoji("\uD83C\uDFF3\uFE0F", "White Flag"),
            Emoji("\uD83C\uDFF4", "Black Flag"),
            Emoji("\uD83C\uDFC1", "Chequered Flag"),
            Emoji("\uD83D\uDEA9", "Triangular Flag"),
            Emoji("\uD83C\uDDFA\uD83C\uDDF3", "United Nations"),
            Emoji("\uD83C\uDDEA\uD83C\uDDF8", "Spain"),
            Emoji("\uD83C\uDDFA\uD83C\uDDF8", "United States"),
            Emoji("\uD83C\uDDEC\uD83C\uDDE7", "United Kingdom"),
            Emoji("\uD83C\uDDEB\uD83C\uDDF7", "France"),
            Emoji("\uD83C\uDDE9\uD83C\uDDEA", "Germany"),
            Emoji("\uD83C\uDDEE\uD83C\uDDF9", "Italy"),
            Emoji("\uD83C\uDDEF\uD83C\uDDF5", "Japan"),
            Emoji("\uD83C\uDDF0\uD83C\uDDF7", "South Korea"),
            Emoji("\uD83C\uDDE8\uD83C\uDDF3", "China"),
            Emoji("\uD83C\uDDE7\uD83C\uDDF7", "Brazil"),
            Emoji("\uD83C\uDDF2\uD83C\uDDFD", "Mexico"),
            Emoji("\uD83C\uDDE6\uD83C\uDDF7", "Argentina"),
            Emoji("\uD83C\uDDE8\uD83C\uDDE6", "Canada"),
            Emoji("\uD83C\uDDEE\uD83C\uDDF3", "India"),
            Emoji("\uD83C\uDDF8\uD83C\uDDEA", "Sweden"),
            Emoji("\uD83C\uDDF3\uD83C\uDDF4", "Norway"),
            Emoji("\uD83C\uDDE6\uD83C\uDDFA", "Australia"),
            Emoji("\uD83C\uDDF3\uD83C\uDDFF", "New Zealand"),
            Emoji("\uD83C\uDDFF\uD83C\uDDE6", "South Africa"),
            Emoji("\uD83C\uDDF7\uD83C\uDDFA", "Russia"),
            Emoji("\uD83C\uDDFA\uD83C\uDDE6", "Ukraine"),
            Emoji("\uD83C\uDDF5\uD83C\uDDF9", "Portugal")
        )
    }

    override suspend fun getCategories(context: Context): List<Category> {
        return listOf(
            Category("faces", "Smileys & People", R.drawable.smile, faces),
            Category("nature", "Animals & Nature", R.drawable.dog, nature),
            Category("food", "Food & Drink", R.drawable.coffee, food),
            Category("activity", "Activities", R.drawable.volleyball, activity),
            Category("travel", "Travel & Places", R.drawable.car_front, travel),
            Category("objects", "Objects", R.drawable.lightbulb, objects),
            Category("symbols", "Symbols", R.drawable.square_radical, symbols),
            Category("flags", "Flags", R.drawable.flag, flags),
        )
    }
}
