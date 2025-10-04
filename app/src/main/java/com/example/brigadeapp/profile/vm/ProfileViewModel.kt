package com.example.brigadeapp.profile.vm

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.core.auth.AuthClient
import com.example.brigadeapp.core.location.LatLng
import com.example.brigadeapp.core.location.LocationClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ProfileUiState(
    val name: String = "Mario",
    val available: Boolean = true,
    val userEmail: String? = null,
    val isOnCampus: Boolean? = null, // null = aun no verificado
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ProfileUiEvent {
    data object ToggleAvailability : ProfileUiEvent
    data object RequestLocation    : ProfileUiEvent
    data object SignOut            : ProfileUiEvent
    data object ClearError         : ProfileUiEvent
}

// ---------- ViewModel ----------

class ProfileViewModel(
    private val auth: AuthClient,
    private val location: LocationClient,
    private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        // Recordar el email del usuario sincronizado con FirebaseAuth
        viewModelScope.launch {
            auth.authState.collect { user ->
                _state.update { it.copy(userEmail = user?.email) }
            }
        }
    }

    fun onEvent(e: ProfileUiEvent) {
        when (e) {
            ProfileUiEvent.ToggleAvailability -> toggleAvailability()
            ProfileUiEvent.SignOut            -> auth.signOut()
            ProfileUiEvent.RequestLocation    -> fetchLocationAndCheckCampus()
            ProfileUiEvent.ClearError         -> _state.update { it.copy(error = null) }
        }
    }

    // ---------- Permisos ----------
    private fun hasFineLocation(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocation(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun canReadLocation() = hasFineLocation() || hasCoarseLocation()

    // ---------- Actions ----------
    private fun toggleAvailability() {
        _state.update { it.copy(available = !it.available) }
        // TODO: persistir esto en repo/backend y loguear telemetría
    }

    private fun fetchLocationAndCheckCampus() = viewModelScope.launch {
        if (!canReadLocation()) {
            _state.update { it.copy(error = "Location permission is required") }
            return@launch
        }
        _state.update { it.copy(isLoading = true, error = null) }

        val loc = location.getLastLocation()
        val onCampus = loc?.let {
            // Usa los defaults del LocationClient (Uniandes + radio 250m)
            location.isInsideRadius(
                point = LatLng(it.latitude, it.longitude)
            )
        }

        _state.update { it.copy(isOnCampus = onCampus, isLoading = false) }
    }
}
