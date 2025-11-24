package com.example.brigadeapp.di

import com.example.brigadeapp.data.repository.BqAnalyticsRepositoryImpl
import com.example.brigadeapp.domain.repository.BqAnalyticsRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object BqAnalyticsModule {

    @Provides
    @Singleton
    fun provideBqAnalyticsRepository(
        db: FirebaseFirestore
    ): BqAnalyticsRepository {
        return BqAnalyticsRepositoryImpl(db)
    }
}
