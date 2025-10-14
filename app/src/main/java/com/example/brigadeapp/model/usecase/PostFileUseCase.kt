package com.example.brigadeapp.model.usecase

import com.example.brigadeapp.model.repository.interfaces.UploadFileRepository
import java.io.File

class PostFileUseCase (
    private val repository: UploadFileRepository
) {

    suspend operator fun invoke(file: File, bucket: String, blob: String): Result<String> {
        return try {
            val urlResult = repository.uploadFile(file, bucket, blob)
            urlResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}