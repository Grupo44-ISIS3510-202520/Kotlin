package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.sensors.LocationSensorImpl
import com.example.brigadeapp.domain.sensors.LocationSensorManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Singleton
    @Provides
    fun provideLocationClient(
        @ApplicationContext context: Context
    ): LocationSensorManager = LocationSensorImpl(context)
}