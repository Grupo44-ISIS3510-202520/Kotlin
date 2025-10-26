package com.example.brigadeapp.core

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Se tiene Auth Client y se la pasa a la UI
interface AuthClient {
    val currentUser: FirebaseUser?
    val authState: Flow<FirebaseUser?>
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser>
    fun signOut()
}

// Auth con Firebase
class FirebaseAuthClient : AuthClient {
    private val auth = FirebaseAuth.getInstance()

    override val currentUser: FirebaseUser? get() = auth.currentUser

    override val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        auth.currentUser ?: error("No user after signIn")
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        auth.currentUser ?: error("No user after register")
    }

    override fun signOut() { auth.signOut() }
}
