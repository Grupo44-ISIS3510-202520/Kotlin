package com.example.brigadeapp.di

import com.example.brigadeapp.data.source.remote.FileUploadApi
import com.example.brigadeapp.data.repository.FileRepositoryImpl
import com.example.brigadeapp.domain.repository.FileRepository
import com.example.brigadeapp.domain.usecase.PostFileUseCase
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
    ): FileRepository = FileRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideUploadFileUseCase(
        repository: FileRepository
    ): PostFileUseCase = PostFileUseCase(repository)
}