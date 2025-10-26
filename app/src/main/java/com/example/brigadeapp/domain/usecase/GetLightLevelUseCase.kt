package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.repository.ContextRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLightLevelUseCase @Inject constructor(
    private val repo: ContextRepository
) {
    operator fun invoke(): Flow<Float> = repo.getLightLevel()
}
