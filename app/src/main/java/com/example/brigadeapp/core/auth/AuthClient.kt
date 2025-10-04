package com.example.brigadeapp.core.auth

import kotlinx.coroutines.flow.StateFlow

/** Contrato neutral (sin Firebase) */
interface AuthClient {
    /** Email del usuario actual o null si no hay sesión */
    val currentUserEmail: String?
    /** Flujo con el email (o null) cuando cambia el estado de auth */
    val authState: StateFlow<String?>
    fun signOut()
}
