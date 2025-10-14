package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.model.repository.implementation.ContextRepositoryImpl
import com.example.brigadeapp.model.repository.implementation.ProtocolRepositoryImpl
import com.example.brigadeapp.model.repository.interfaces.ContextRepository
import com.example.brigadeapp.model.repository.interfaces.ProtocolRepository
import com.example.brigadeapp.model.usecase.GetLightLevelUseCase
import com.example.brigadeapp.model.usecase.GetUpdatedProtocolsUseCase
import com.example.brigadeapp.view.sensors.LightSensorManagerImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("protocols")
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideProtocolRepository(
        firestore: FirebaseFirestore
    ): ProtocolRepository = ProtocolRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideContextRepository(
        @ApplicationContext app: Context
    ): ContextRepository = ContextRepositoryImpl(LightSensorManagerImpl(app))

    @Provides
    @Singleton
    fun provideLightLevelUseCase(repo: ContextRepository): GetLightLevelUseCase =
        GetLightLevelUseCase(repo)

    @Provides
    @Singleton
    fun provideUpdatedProtocolsUseCase(repo: ProtocolRepository): GetUpdatedProtocolsUseCase =
        GetUpdatedProtocolsUseCase(repo)
}

