package com.example.brigadeapp.data.services.remote

import android.content.Context
import com.example.brigadeapp.helpers.ApiKeys
import com.example.brigadeapp.data.services.OpenAIService
import com.example.brigadeapp.data.source.remote.OpenAIApi
import com.example.brigadeapp.domain.entity.ChatRequest
import com.example.brigadeapp.domain.entity.Message
import javax.inject.Inject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenAIRemoteService @Inject constructor(
    private val context: Context
) : OpenAIService {

    private val api: OpenAIApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${ApiKeys.OPENAI_KEY}")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        retrofit.create(OpenAIApi::class.java)
    }

    override suspend fun request(prompt: String): String {
        val response = api.request(
            ChatRequest(
                messages = listOf(
                    Message("user", prompt)
                )
            )
        )

        val content = response.choices.firstOrNull()?.message?.content ?: ""
        return content
    }
}
