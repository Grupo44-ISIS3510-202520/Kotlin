package com.example.brigadeapp.core.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface AuthClient {
    val currentUser: FirebaseUser?
    val authState: Flow<FirebaseUser?>
    fun signOut()
}

class FirebaseAuthClient : AuthClient {
    private val auth = FirebaseAuth.getInstance()

    override val currentUser: FirebaseUser? get() = auth.currentUser

    override val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun signOut() { auth.signOut() }
}
