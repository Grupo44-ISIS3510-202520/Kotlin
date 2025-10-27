package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.entity.AuthUser
import com.example.brigadeapp.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val repo: AuthRepository) {
    operator fun invoke(): AuthUser? = repo.current()
}
