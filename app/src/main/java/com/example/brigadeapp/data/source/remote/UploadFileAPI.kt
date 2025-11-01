package com.example.brigadeapp.data.source.remote

import com.example.brigadeapp.domain.entity.FileUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileUploadApi {
    @Multipart
    @POST("upload-file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("bucket") bucket: RequestBody,
        @Part("blob") blob: RequestBody
    ): Response<FileUploadResponse>
}