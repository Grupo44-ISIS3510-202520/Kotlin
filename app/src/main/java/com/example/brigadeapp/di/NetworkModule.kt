package com.example.brigadeapp.di

import com.example.brigadeapp.data.api.FileUploadApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://file-uploader-973554845747.europe-west1.run.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideFileUploadApi(retrofit: Retrofit): FileUploadApi {
        return retrofit.create(FileUploadApi::class.java)
    }
}