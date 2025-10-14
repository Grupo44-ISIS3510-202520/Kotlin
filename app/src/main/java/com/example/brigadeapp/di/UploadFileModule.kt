package com.example.brigadeapp.di

import com.example.brigadeapp.model.data.api.FileUploadApi
import com.example.brigadeapp.model.repository.implementation.UploadRepositoryImpl
import com.example.brigadeapp.model.repository.interfaces.UploadFileRepository
import com.example.brigadeapp.model.usecase.PostFileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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