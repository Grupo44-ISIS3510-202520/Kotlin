package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.entity.AuthUser
import com.example.brigadeapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase(private val repo: AuthRepository) {
    operator fun invoke(): Flow<AuthUser?> = repo.observe()
}
