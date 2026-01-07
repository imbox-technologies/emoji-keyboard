package com.davidperi.emojikeyboardtest.model

data class ChatMessage(
    val id: Long,
    val text: String,
    val isMe: Boolean = true
)