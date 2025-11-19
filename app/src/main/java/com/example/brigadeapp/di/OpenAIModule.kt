package com.example.brigadeapp.di

import com.example.brigadeapp.data.adapter.OpenAIHttpAdapter
import com.example.brigadeapp.domain.repository.OpenAIRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OpenAIModule {
    @Binds
    @Singleton
    abstract fun bindOpenAIRepository(impl: OpenAIHttpAdapter): OpenAIRepository
}
