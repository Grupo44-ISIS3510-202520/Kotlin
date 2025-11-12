package com.example.brigadeapp.data.source.remote

import com.example.brigadeapp.domain.entity.ChatRequest
import com.example.brigadeapp.domain.entity.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST


interface OpenAIApi {
    @POST("chat/completions")
    suspend fun getInstructions(
        @Body request: ChatRequest
    ): ChatResponse
}