package com.example.brigadeapp.model.repository.interfaces

import java.io.File

interface UploadFileRepository {
    suspend fun uploadFile(file: File, bucket: String, blob: String): Result<String>
}