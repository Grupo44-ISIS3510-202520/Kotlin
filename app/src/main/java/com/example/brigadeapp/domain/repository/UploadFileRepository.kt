package com.example.brigadeapp.domain.repository

import java.io.File

interface UploadFileRepository {
    suspend fun uploadFile(file: File, bucket: String, blob: String): Result<String>
}