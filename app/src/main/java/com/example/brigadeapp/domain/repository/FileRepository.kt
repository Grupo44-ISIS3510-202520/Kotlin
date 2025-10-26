package com.example.brigadeapp.domain.repository

import java.io.File

interface FileRepository {
    suspend fun uploadFile(file: File, bucket: String, blob: String): Result<String>
}