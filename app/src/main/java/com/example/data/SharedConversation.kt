package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SharedMessageDto(
    val role: String,
    val content: String,
    val modelUsed: String? = null
)

@JsonClass(generateAdapter = true)
data class SharedConversation(
    val title: String,
    val messages: List<SharedMessageDto>,
    val authorLabel: String? = null
)
