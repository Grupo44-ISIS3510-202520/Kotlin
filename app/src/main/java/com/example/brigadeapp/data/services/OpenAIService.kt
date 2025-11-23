package com.example.brigadeapp.data.services

interface OpenAIService {
    suspend fun request(prompt: String): String
}