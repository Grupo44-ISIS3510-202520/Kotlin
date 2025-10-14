package com.example.brigadeapp.model.usecase

import com.example.brigadeapp.model.repository.interfaces.ContextRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLightLevelUseCase @Inject constructor(
    private val repo: ContextRepository
) {
    operator fun invoke(): Flow<Float> = repo.getLightLevel()
}
