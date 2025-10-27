package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.entity.Alert
import com.example.brigadeapp.domain.repository.AlertsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlertsUseCase @Inject constructor(
    private val repo: AlertsRepository
) {
    operator fun invoke(): Flow<List<Alert>> {
        return repo.getAlerts()
    }
}
