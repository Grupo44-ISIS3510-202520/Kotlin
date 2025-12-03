package com.example.brigadeapp.data.services.local

import android.content.Context
import android.util.Log
import com.example.brigadeapp.data.services.OpenAIService
import javax.inject.Inject
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val RESPONSES_FILENAME = "openai_responses.jsonl"

class OpenAILocal @Inject constructor(
	private val context: Context
) : OpenAIService {

	override suspend fun request(prompt: String): String {
		return try {
			withContext(Dispatchers.IO) {
				val file = File(context.filesDir, RESPONSES_FILENAME)
				if (!file.exists()) return@withContext ""
				val lines = file.readLines()
				var lastResponse: String? = null
				var lastTimestamp: Long = -1
				for (line in lines) {
					try {
						val obj = JSONObject(line)
						val p = obj.optString("prompt", null)
						if (p == prompt) {
							val ts = obj.optLong("timestamp", -1)
							if (ts >= lastTimestamp) {
								lastTimestamp = ts
								lastResponse = obj.optString("response", "")
							}
						}
					} catch (e: Exception) {
						throw Exception("Error parsing response: " + e.message)
					}
				}
				return@withContext lastResponse ?: ""
			}
		} catch (e: Exception) {
			throw Exception("Error getting response: " + e.message)
		}
	}

	suspend fun saveResponse(prompt: String, response: String): Boolean {
		return try {
			withContext(Dispatchers.IO) {
                Log.d("OpenAILocal", "Saving response for prompt: $prompt")
				val file = File(context.filesDir, RESPONSES_FILENAME)
				if (!file.exists()) file.createNewFile()
				val obj = JSONObject()
				obj.put("timestamp", System.currentTimeMillis())
				obj.put("prompt", prompt)
				obj.put("response", response)
				file.appendText(obj.toString())
				file.appendText("\n")
			}
			true
		} catch (e: Exception) {
			throw Exception("Error saving response: " + e.message)
		}
	}

	suspend fun getLastResponseTimestamp(prompt: String): Long {
		return try {
			withContext(Dispatchers.IO) {
				val file = File(context.filesDir, RESPONSES_FILENAME)
				if (!file.exists()) return@withContext 0L
				val lines = file.readLines()
				var lastTimestamp: Long = 0L
				for (line in lines) {
					try {
						val obj = JSONObject(line)
						val p = obj.optString("prompt", null)
						if (p == prompt) {
							val ts = obj.optLong("timestamp", -1)
							if (ts > lastTimestamp) {
								lastTimestamp = ts
							}
						}
					} catch (e: Exception) {
						// Skip malformed lines
					}
				}
				return@withContext lastTimestamp
			}
		} catch (e: Exception) {
			0L
		}
	}
}