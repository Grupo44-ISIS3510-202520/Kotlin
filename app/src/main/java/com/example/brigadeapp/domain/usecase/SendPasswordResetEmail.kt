package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.repository.AuthRepository

class SendPasswordResetEmail(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String) = repo.sendPasswordResetEmail(email.trim())
}
