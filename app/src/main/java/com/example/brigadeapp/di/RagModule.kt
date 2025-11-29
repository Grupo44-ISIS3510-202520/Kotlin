package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.local.RagCache
import com.example.brigadeapp.data.local.RagLocalStorage
import com.example.brigadeapp.data.repository.RagRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RagModule {

    @Provides
    @Singleton
    fun provideRagCache(): RagCache {
        return RagCache()
    }

    @Provides
    @Singleton
    fun provideRagLocalStorage(
        @ApplicationContext context: Context
    ): RagLocalStorage {
        return RagLocalStorage(context)
    }

    @Provides
    @Singleton
    fun provideRagRepository(
        @ApplicationContext context: Context,
        httpClient: HttpClient,
        cache: RagCache,
        localStorage: RagLocalStorage
    ): RagRepository {
        return RagRepository(context, httpClient, cache, localStorage)
    }
}