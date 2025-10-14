package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.model.repository.implementation.ReportRepositoryImpl
import com.example.brigadeapp.model.repository.interfaces.ReportRepository
import com.example.brigadeapp.model.usecase.PostReportUseCase
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReportModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics = Firebase.analytics

    @Provides
    @Singleton
    fun provideReportRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ReportRepository = ReportRepositoryImpl(firestore, storage)

    @Provides
    @Singleton
    fun provideSubmitReportUseCase(
        repository: ReportRepository
    ): PostReportUseCase = PostReportUseCase(repository)
}
