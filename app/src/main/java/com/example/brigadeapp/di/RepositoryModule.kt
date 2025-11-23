package com.example.brigadeapp.di

import com.example.brigadeapp.data.repository.OpenAIRepositoryImpl
import com.example.brigadeapp.domain.repository.OpenAIRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.InstallIn
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOpenAIRepository(
        impl: OpenAIRepositoryImpl
    ): OpenAIRepository
}
