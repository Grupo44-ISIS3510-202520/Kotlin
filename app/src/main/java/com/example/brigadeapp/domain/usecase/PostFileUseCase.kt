package com.example.brigadeapp.domain.usecase

import android.util.Log
import com.example.brigadeapp.domain.repository.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class PostFileUseCase @Inject constructor(
    private val repository: FileRepository
) {

    suspend operator fun invoke(file: File, bucket: String, blob: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val urlResult = repository.uploadFile(file = file, bucket = bucket, blob = blob)
                urlResult
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}