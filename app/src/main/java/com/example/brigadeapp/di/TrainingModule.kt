package com.example.brigadeapp.di

import com.example.brigadeapp.domain.repository.TrainingRepository
import com.example.brigadeapp.data.repository.TrainingRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrainingModule {

    @Provides @Singleton
    fun provideTrainingRepository(
        db: FirebaseFirestore,
        auth: FirebaseAuth
    ): TrainingRepository = TrainingRepositoryImpl(db, auth)
}
