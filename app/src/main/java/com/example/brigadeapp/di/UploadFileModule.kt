package com.example.brigadeapp.di

import com.example.brigadeapp.data.api.FileUploadApi
import com.example.brigadeapp.data.repository.UploadRepositoryImpl
import com.example.brigadeapp.domain.repository.UploadFileRepository
import com.example.brigadeapp.domain.usecase.PostFileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UploadFileModule {
    @Provides
    @Singleton
    fun provideUploadFileRepository(
        api: FileUploadApi
    ): UploadFileRepository = UploadRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideUploadFileUseCase(
        repository: UploadFileRepository
    ): PostFileUseCase = PostFileUseCase(repository)
}