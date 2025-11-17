package com.example.brigadeapp.domain.entity

// Request
data class ChatRequest(
    val model: String = "gpt-4.1-mini",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

// Response
data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)