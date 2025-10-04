package com.example.brigadeapp.profile.vm

import android.Manifest
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
    private val appContext: android.content.Context,
    // 👇 Fallbacks para desarrollo (no rompes nada en prod)
    private val devFallbackEmail: String? = null,
    private val devMockLocation: LatLng? = null,
    private val campusCenter: LatLng = LatLng(4.6026783, -74.0653568),
    private val campusRadiusMeters: Double = 250.0
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        // Si hay Auth real, escucha; si no, usa fallbackEmail para que la UI “parezca logueada”.
        viewModelScope.launch {
            auth.authState.collect { user ->
                val email = user?.email ?: devFallbackEmail
                _state.update { it.copy(userEmail = email) }
            }
        }
        if (auth.currentUser == null && devFallbackEmail != null) {
            _state.update { it.copy(userEmail = devFallbackEmail) }
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
            loc != null -> LatLng(loc.latitude, loc.longitude)
            // 👇 En emulador suele ser null; usa mock si te lo pasaron (devMockLocation)
            devMockLocation != null -> devMockLocation
            else -> null
        }

        val onCampus = point?.let {
            location.isInsideRadius(
                point = it,
                center = campusCenter,
                radiusMeters = campusRadiusMeters
            )
        }

        _state.update { it.copy(isOnCampus = onCampus, isLoading = false) }
    }
}
