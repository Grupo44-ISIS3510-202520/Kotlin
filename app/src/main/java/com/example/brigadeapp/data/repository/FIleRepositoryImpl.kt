package com.example.brigadeapp.data.repository

import com.example.brigadeapp.data.source.remote.FileUploadApi
import com.example.brigadeapp.domain.repository.FileRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class FIleRepositoryImpl @Inject constructor(
    private val api: FileUploadApi
): FileRepository {

    override suspend fun uploadFile(
        file: File,
        bucket: String,
        blob: String
    ): Result<String> {
        return try {
            val fileRequestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, fileRequestBody)

            val bucketPart = bucket.toRequestBody("text/plain".toMediaTypeOrNull())
            val blobPart = blob.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadFile(bucketPart, blobPart, filePart)

            if (response.isSuccessful) {
                response.body()?.url?.let { Result.success(it) }
                    ?: Result.failure(Exception("No se recibió URL en la respuesta"))
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}