package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.entity.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observe(): Flow<AuthUser?>
    fun current(): AuthUser?

    suspend fun signInWithEmail(email: String, password: String): AuthUser
    suspend fun registerWithEmail(email: String, password: String): AuthUser

    suspend fun sendEmailVerification()
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun reload()
    suspend fun signOut()
}
