package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.entity.AuthUser
import com.example.brigadeapp.domain.repository.AuthRepository

class SignInWithEmail(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthUser =
        repo.signInWithEmail(email, password)
}
