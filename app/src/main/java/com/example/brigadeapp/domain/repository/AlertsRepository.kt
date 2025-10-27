package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.entity.Alert
import kotlinx.coroutines.flow.Flow

interface AlertsRepository {

    fun getAlerts(): Flow<List<Alert>>
}

