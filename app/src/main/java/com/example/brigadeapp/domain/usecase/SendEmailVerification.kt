package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.repository.AuthRepository

class SendEmailVerification(
    private val repo: AuthRepository
) {
    suspend operator fun invoke() = repo.sendEmailVerification()
}
