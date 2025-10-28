package com.example.brigadeapp.domain.usecase

import android.util.Log
import com.example.brigadeapp.domain.repository.FileRepository
import java.io.File

class PostFileUseCase (
    private val repository: FileRepository
) {

    suspend operator fun invoke(file: File, bucket: String, blob: String): Result<String> {
        return try {
            val urlResult = repository.uploadFile(file = file, bucket = bucket, blob = blob)
            urlResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}