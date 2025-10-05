package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.repository.ContextRepository
import kotlinx.coroutines.flow.Flow

/** Caso de uso: observa el nivel de luz (lux) en tiempo real. */
class GetLightLevelUseCase(
    private val repo: ContextRepository
) {
    operator fun invoke(): Flow<Float> = repo.getLightLevel()
}
