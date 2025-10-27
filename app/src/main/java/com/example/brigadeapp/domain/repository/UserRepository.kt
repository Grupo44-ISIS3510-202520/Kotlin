package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.entity.UserProfile

interface UserRepository {
    suspend fun saveProfile(profile: UserProfile)           // upsert users/{uid}
    suspend fun getProfile(uid: String): UserProfile?
}
