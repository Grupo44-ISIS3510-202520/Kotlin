package com.example.brigadeapp.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.core.auth.AuthClient
import com.example.brigadeapp.core.location.LatLng
import com.example.brigadeapp.core.location.LocationClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "Mario",
    val available: Boolean = true,
    val userEmail: String? = null,
    val isOnCampus: Boolean? = null,
    val userPoint: LatLng? = null,
    val insideCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ProfileUiEvent {
    data object ToggleAvailability : ProfileUiEvent
    data object RequestLocation    : ProfileUiEvent
    data object SignOut            : ProfileUiEvent
    data object ClearError         : ProfileUiEvent
}

class ProfileViewModel(
    private val auth: AuthClient,
    private val location: LocationClient,
    private val appContext: Context,
    // Fallbacks de desarrollo (útiles cuando no hay backend / lastLocation = null)
    private val devFallbackEmail: String? = null,
    private val devMockLocation: LatLng? = null,
    private val campusCenter: LatLng = LatLng(4.6026783, -74.0653568),
    private val campusRadiusMeters: Double = 250.0
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        // 1) Estado inicial (Firebase recuerda sesión)
        val initialEmail = auth.currentUser?.email ?: devFallbackEmail
        if (initialEmail != null) {
            _state.update { it.copy(userEmail = initialEmail) }
        }

        // 2) Suscribirse a cambios de sesión (mapear FirebaseUser? -> String?)
        viewModelScope.launch {
            auth.authState.collect { user ->
                val email = user?.email ?: devFallbackEmail
                _state.update { it.copy(userEmail = email) }
            }
        }
    }

    fun onEvent(e: ProfileUiEvent) {
        when (e) {
            ProfileUiEvent.ToggleAvailability -> _state.update { it.copy(available = !it.available) }
            ProfileUiEvent.SignOut           -> auth.signOut()
            ProfileUiEvent.RequestLocation   -> getLocation()
            ProfileUiEvent.ClearError        -> _state.update { it.copy(error = null) }
        }
    }

    private fun hasFineLocation(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocation(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun canReadLocation() = hasFineLocation() || hasCoarseLocation()

    private fun getLocation() = viewModelScope.launch {
        if (!canReadLocation()) {
            _state.update { it.copy(error = "Missing location permission") }
            return@launch
        }
        _state.update { it.copy(isLoading = true, error = null) }

        val loc = location.getLastLocation()
        val point: LatLng? = when {
            loc != null           -> LatLng(loc.latitude, loc.longitude)
            devMockLocation != null -> devMockLocation
            else                  -> null
        }

        val onCampus = point?.let {
            location.isInsideRadius(
                point = it,
                center = campusCenter,
                radiusMeters = campusRadiusMeters
            )
        }

        _state.update {
            it.copy(
                userPoint = point,
                isOnCampus = onCampus,
                insideCount = if (onCampus == true) 1 else 0,
                isLoading = false
            )
        }
    }
}
