package com.davidperi.emojikeyboard.utils

fun lerp(start: Int, stop: Int, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}