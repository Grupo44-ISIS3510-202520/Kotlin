package com.example.brigadeapp.di

import com.example.brigadeapp.data.repository.ReportRepositoryImpl
import com.example.brigadeapp.domain.repository.ReportRepository
import com.example.brigadeapp.domain.usecase.SubmitReportUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideReportRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ReportRepository = ReportRepositoryImpl(firestore, storage)

    @Provides
    @Singleton
    fun provideSubmitReportUseCase(
        repository: ReportRepository
    ): SubmitReportUseCase = SubmitReportUseCase(repository)
}
