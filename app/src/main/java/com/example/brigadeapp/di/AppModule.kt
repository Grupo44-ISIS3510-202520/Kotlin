package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.repository.AlertsRepositoryImpl
import com.example.brigadeapp.data.repository.ProtocolRepositoryImpl
import com.example.brigadeapp.data.repository.ContextRepositoryImpl
import com.example.brigadeapp.data.sensors.LightSensorManagerImpl
import com.example.brigadeapp.domain.repository.AlertsRepository
import com.example.brigadeapp.domain.repository.ProtocolRepository
import com.example.brigadeapp.domain.repository.ContextRepository
import com.example.brigadeapp.domain.usecase.GetAlertsUseCase
import com.example.brigadeapp.domain.usecase.GetLightLevelUseCase
import com.example.brigadeapp.domain.usecase.GetUpdatedProtocolsUseCase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.brigadeapp.data.datastore.ProtocolVersionDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.brigadeapp.domain.utils.CachedFileDownloader

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideLightSensorManager(@ApplicationContext context: Context): LightSensorManagerImpl {
        return LightSensorManagerImpl(context)
    }

    @Provides
    @Singleton
    fun provideContextRepository(
        @ApplicationContext context: Context,
        sensorManager: LightSensorManagerImpl
    ): ContextRepository {
        return ContextRepositoryImpl(sensorManager)
    }


    @Provides
    @Singleton
    fun provideGetLightLevelUseCase(repo: ContextRepository): GetLightLevelUseCase {
        return GetLightLevelUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideGetUpdatedProtocolsUseCase(repo: ProtocolRepository): GetUpdatedProtocolsUseCase {
        return GetUpdatedProtocolsUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideAlertsRepository(firestore: FirebaseFirestore): AlertsRepository {
        return AlertsRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideGetAlertsUseCase(repo: AlertsRepository): GetAlertsUseCase {
        return GetAlertsUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideProtocolVersionDataStore(@ApplicationContext context: Context): ProtocolVersionDataStore {
        return ProtocolVersionDataStore(context)
    }

    @Provides
    @Singleton
    fun provideProtocolRepository(
        firestore: FirebaseFirestore,
        versionDataStore: ProtocolVersionDataStore,

        // --- 1. AÑADE ESTOS DOS PARÁMETROS ---
        fileDownloader: CachedFileDownloader,
        @ApplicationContext context: Context

    ): ProtocolRepository {

        // --- 2. PÁSALOS AL CONSTRUCTOR ---
        return ProtocolRepositoryImpl(firestore, versionDataStore, fileDownloader, context)
    }

}