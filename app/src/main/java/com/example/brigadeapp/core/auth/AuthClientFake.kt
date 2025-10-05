package com.example.brigadeapp.core.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthClientFake : AuthClient {
    private val _user = MutableStateFlow<FirebaseUser?>(null)
    override val currentUser: FirebaseUser? get() = _user.value
    override val authState: Flow<FirebaseUser?> = _user.asStateFlow()
    override suspend fun signInWithEmail(email: String, password: String) = Result.failure<FirebaseUser>(UnsupportedOperationException("Fake"))
    override suspend fun registerWithEmail(email: String, password: String) = Result.failure<FirebaseUser>(UnsupportedOperationException("Fake"))
    override fun signOut() { _user.value = null }
}
