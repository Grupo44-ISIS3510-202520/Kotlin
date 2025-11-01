package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.sensors.ConnectivityObserver
import kotlinx.coroutines.flow.Flow

class ObserveConnectivityUseCase (
    private val connectivityObserver: ConnectivityObserver
) {
    operator fun invoke(): Flow<Boolean> = connectivityObserver.observe()
}