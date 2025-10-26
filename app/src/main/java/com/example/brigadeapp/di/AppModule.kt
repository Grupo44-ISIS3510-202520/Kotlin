package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.repository.ProtocolRepositoryImpl
import com.example.brigadeapp.data.repository.ContextRepositoryImpl
import com.example.brigadeapp.data.sensors.LightSensorManagerImpl
import com.example.brigadeapp.domain.repository.ProtocolRepository
import com.example.brigadeapp.domain.repository.ContextRepository
import com.example.brigadeapp.domain.usecase.GetLightLevelUseCase
import com.example.brigadeapp.domain.usecase.GetUpdatedProtocolsUseCase
import com.google.firebase.analytics.FirebaseAnalytics
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
object AppModule {

    // 1️⃣ Proveedor de Firestore (Simplificado)
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        // Hilt se encarga de que sea Singleton
        return FirebaseFirestore.getInstance()
    }

    // ✅ Proveedor de Firebase Storage
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    // ✅ Proveedor de Firebase Analytics
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    // 2️⃣ Sensor de luz
    @Provides
    @Singleton
    fun provideLightSensorManager(@ApplicationContext context: Context): LightSensorManagerImpl {
        return LightSensorManagerImpl(context)
    }

    // 3️⃣ Repository de contexto (sensor)
    @Provides
    @Singleton
    fun provideContextRepository(
        @ApplicationContext context: Context,
        sensorManager: LightSensorManagerImpl
    ): ContextRepository {
        return ContextRepositoryImpl(sensorManager)
    }

    // 4️⃣ Repository de protocolos (Firestore)
    @Provides
    @Singleton
    fun provideProtocolRepository(firestore: FirebaseFirestore): ProtocolRepository {
        return ProtocolRepositoryImpl(firestore)
    }

    // 5️⃣ Caso de uso: sensor de luz
    @Provides
    @Singleton
    fun provideGetLightLevelUseCase(repo: ContextRepository): GetLightLevelUseCase {
        return GetLightLevelUseCase(repo)
    }

    // 6️⃣ Caso de uso: protocolos actualizados
    @Provides
    @Singleton
    fun provideGetUpdatedProtocolsUseCase(repo: ProtocolRepository): GetUpdatedProtocolsUseCase {
        return GetUpdatedProtocolsUseCase(repo)
    }
}