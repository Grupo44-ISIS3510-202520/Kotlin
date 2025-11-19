package com.example.brigadeapp.data.adapter

import com.example.brigadeapp.core.ApiKeys
import com.example.brigadeapp.domain.repository.OpenAIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import javax.inject.Inject

class OpenAIHttpAdapter @Inject constructor(
    private val okHttp: OkHttpClient
) : OpenAIRepository {

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    override suspend fun getInstructions(prompt: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val messages = JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", prompt)
                )

                val bodyJson = JSONObject()
                    .put("model", "gpt-3.5-turbo")
                    .put("messages", messages)

                val reqBody = bodyJson.toString().toRequestBody(jsonMedia)

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(reqBody)
                    .addHeader("Authorization", "Bearer ${ApiKeys.OPENAI_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()

                okHttp.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext emptyList()
                    val text = resp.body?.string().orEmpty()
                    val parsed = JSONObject(text)
                    val choices = parsed.optJSONArray("choices")
                    val content = choices
                        ?.optJSONObject(0)
                        ?.optJSONObject("message")
                        ?.optString("content") ?: ""

                    return@withContext content.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                }
            } catch (e: Exception) {
                // On error return empty list; consumers may fallback to defaults
                return@withContext emptyList()
            }
        }
}
