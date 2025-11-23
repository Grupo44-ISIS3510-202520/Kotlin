package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.source.local.sensors.ConnectivityManagerObserver
import com.example.brigadeapp.domain.sensors.ConnectivityObserver
import com.example.brigadeapp.domain.usecase.ObserveConnectivityUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectivityModule {

    @Singleton
    @Provides
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver = ConnectivityManagerObserver(context)

    @Singleton
    @Provides
    fun provideObserveConnectivityUseCase(
        connectivityObserver: ConnectivityObserver
    ): ObserveConnectivityUseCase = ObserveConnectivityUseCase(connectivityObserver)
}