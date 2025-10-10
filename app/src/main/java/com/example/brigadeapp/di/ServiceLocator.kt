package com.example.brigadeapp.di

import android.content.Context
import com.example.brigadeapp.data.repository.ContextRepositoryImpl
import com.example.brigadeapp.domain.repository.ContextRepository
import com.example.brigadeapp.domain.usecase.GetLightLevelUseCase
import com.example.brigadeapp.presentation.viewmodel.ProtocolsViewModel
import com.example.brigadeapp.sensors.LightSensorManagerImpl

object ServiceLocator {

    // Repository
    private fun contextRepository(app: Context): ContextRepository =
        ContextRepositoryImpl(LightSensorManagerImpl(app.applicationContext))

    // Use case
    fun getLightLevelUseCase(app: Context) =
        GetLightLevelUseCase(contextRepository(app))

    // ViewModel (solo sensor)
    fun protocolsViewModel(app: Context): ProtocolsViewModel =
        ProtocolsViewModel(
            getLightLevel = getLightLevelUseCase(app)
            // si luego quieres inyectar getUpdatedProtocols, añade el parámetro aquí
        )
}
