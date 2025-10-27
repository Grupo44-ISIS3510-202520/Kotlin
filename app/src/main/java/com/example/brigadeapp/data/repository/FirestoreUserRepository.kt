package com.example.brigadeapp.data.repository

import com.example.brigadeapp.domain.entity.UserProfile
import com.example.brigadeapp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserRepository @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {

    private val col get() = db.collection("users")

    override suspend fun saveProfile(profile: UserProfile) {
        col.document(profile.uid).set(
            mapOf(
                "email" to profile.email,
                "name" to profile.name,
                "lastName" to profile.lastName,
                "uniandesCode" to profile.uniandesCode,
                "bloodGroup" to profile.bloodGroup,
                "role" to profile.role
            )
        ).await()
    }

    override suspend fun getProfile(uid: String): UserProfile? {
        val s = col.document(uid).get().await()
        if (!s.exists()) return null
        return UserProfile(
            uid = uid,
            email = s.getString("email").orEmpty(),
            name = s.getString("name").orEmpty(),
            lastName = s.getString("lastName").orEmpty(),
            uniandesCode = s.getString("uniandesCode").orEmpty(),
            bloodGroup = s.getString("bloodGroup").orEmpty(),
            role = s.getString("role").orEmpty()
        )
    }
}
