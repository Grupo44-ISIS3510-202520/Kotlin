package com.example.brigadeapp.domain.utils

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedFileDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "CachedFileDownloader"
    }

    suspend fun downloadFile(url: String, fileName: String): Result<File> = withContext(Dispatchers.IO) {
        val localFile = File(context.cacheDir, fileName)
        if (localFile.exists()) {
            Log.d(TAG, "Cache HIT (L1 File): $fileName")
            return@withContext Result.success(localFile)
        }

        try {
            Log.d(TAG, "Cache MISS (L1 File). Downloading: $url")

            val request = Request.Builder()
                .url(url)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Download failed: ${response.code}")
                )
            }

            response.body?.byteStream()?.use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }

            val sizeKB = localFile.length() / 1024
            Log.d(TAG, "Downloaded from network: $fileName - ${sizeKB}KB")

            Result.success(localFile)

        } catch (e: Exception) {
            Log.e(TAG, "Download error: $url", e)
            Result.failure(e)
        }
    }

    fun getCacheSize(): Long {
        return context.cacheDir.listFiles { file ->
            file.isFile && (file.name.endsWith(".pdf") || file.name.contains("_v"))
        }?.sumOf { it.length() } ?: 0L
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        try {
            okHttpClient.cache?.evictAll()

            context.cacheDir.listFiles { file ->
                file.isFile && (file.name.endsWith(".pdf") || file.name.contains("_v"))
            }?.forEach { it.delete() }

            Log.d(TAG, "Both caches (L1 + L2) cleared")

        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
}
