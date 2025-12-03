package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.repository.ReportRepositoryImpl
import com.example.brigadeapp.data.services.local.ReportLocalService
import com.example.brigadeapp.data.services.local.SyncPreferencesService
import com.example.brigadeapp.data.services.remote.ReportRemoteService
import com.example.brigadeapp.domain.repository.ReportRepository
import com.example.brigadeapp.domain.usecase.PostReportUseCase
import com.example.brigadeapp.domain.usecase.GetReportsUseCase
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
    fun provideReportRemoteService(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        @ApplicationContext context: Context
    ): ReportRemoteService = ReportRemoteService(firestore, storage, context)

    @Provides
    @Singleton
    fun provideReportLocalService(
        @ApplicationContext context: Context
    ): ReportLocalService = ReportLocalService(context)

    @Provides
    @Singleton
    fun provideReportRepository(
        @ApplicationContext context: Context,
        remote: ReportRemoteService,
        local: ReportLocalService,
        syncPreferences: SyncPreferencesService
    ): ReportRepository = ReportRepositoryImpl(context, remote, local, syncPreferences)

    @Provides
    @Singleton
    fun provideSubmitReportUseCase(
        repository: ReportRepository
    ): PostReportUseCase = PostReportUseCase(repository)

    @Provides
    @Singleton
    fun provideGetReportsUseCase(
        repository: ReportRepository
    ): GetReportsUseCase = GetReportsUseCase(repository)
}
