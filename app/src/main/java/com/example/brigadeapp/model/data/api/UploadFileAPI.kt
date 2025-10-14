package com.example.brigadeapp.model.data.api

import com.example.brigadeapp.model.domain.FileUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileUploadApi {
    @Multipart
    @POST("/")
    suspend fun uploadFile(
        @Part("bucket") bucket: RequestBody,
        @Part("blob") blob: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<FileUploadResponse>
}