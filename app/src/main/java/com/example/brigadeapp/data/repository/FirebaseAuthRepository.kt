package com.example.brigadeapp.data.repository

import com.example.brigadeapp.domain.entity.AuthUser
import com.example.brigadeapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    private fun map(): AuthUser? {
        val u = auth.currentUser ?: return null
        return AuthUser(
            uid = u.uid,
            email = u.email.orEmpty(),
            emailVerified = u.isEmailVerified
        )
    }

    override fun observe(): Flow<AuthUser?> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { trySend(map()) }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }

    override fun current(): AuthUser? = map()

    override suspend fun signInWithEmail(email: String, password: String): AuthUser {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        return map() ?: error("No user after sign in")
    }

    override suspend fun registerWithEmail(email: String, password: String): AuthUser {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        return map() ?: error("No user after register")
    }

    override suspend fun sendEmailVerification() {
        auth.currentUser?.let { if (!it.isEmailVerified) it.sendEmailVerification().await() }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun reload() {
        auth.currentUser?.reload()?.await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
