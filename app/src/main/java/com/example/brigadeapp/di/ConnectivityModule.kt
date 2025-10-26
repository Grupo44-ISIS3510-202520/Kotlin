package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.connectivity.ConnectivityManagerConnectivityObserver
import com.example.brigadeapp.domain.connectivity.ConnectivityObserver
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
    ): ConnectivityObserver = ConnectivityManagerConnectivityObserver(context)

    @Singleton
    @Provides
    fun provideObserveConnectivityUseCase(
        connectivityObserver: ConnectivityObserver
    ): ObserveConnectivityUseCase = ObserveConnectivityUseCase(connectivityObserver)
}