package com.example.brigadeapp.data.repository

import android.content.Context
import com.example.brigadeapp.core.ApiKeys
import com.example.brigadeapp.data.source.remote.OpenAIApi
import com.example.brigadeapp.domain.entity.ChatRequest
import com.example.brigadeapp.domain.entity.Message
import com.example.brigadeapp.domain.repository.OpenAIRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class OpenAIImpl @Inject constructor(
    @ApplicationContext private val context: Context
): OpenAIRepository {
    private val api: OpenAIApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${ApiKeys.OPENAI_API_KEY}")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        retrofit.create(OpenAIApi::class.java)
    }

    override suspend fun getInstructions(prompt: String): List<String> {
        val response = api.getInstructions(
            ChatRequest(
                messages = listOf(
                    Message("user", prompt)
                )
            )
        )

        val content = response.choices.firstOrNull()?.message?.content ?: ""
        return content.split("\n").filter { it.isNotBlank() }
    }
}