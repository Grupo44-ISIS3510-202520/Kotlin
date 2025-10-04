package com.example.brigadeapp.core.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake simple: no sesión real. authState siempre null.
 * (El VM tendrá un correo de fallback para pruebas.)
 */
class AuthClientFake : AuthClient {
    override val currentUser: FirebaseUser? = null
    override val authState: Flow<FirebaseUser?> = flowOf(null)
    override fun signOut() { /* no-op */ }
}
