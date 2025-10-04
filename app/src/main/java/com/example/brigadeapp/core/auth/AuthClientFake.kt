package com.example.brigadeapp.core.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthClientFake(initialEmail: String? = "dev@mock.local") : AuthClient {
    private val _auth = MutableStateFlow(initialEmail)
    override val authState: StateFlow<String?> = _auth.asStateFlow()
    override val currentUserEmail: String? get() = _auth.value
    override fun signOut() { _auth.value = null }
}
