package com.example.brigadeapp.viewmodel.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.entity.AuthClient
import com.example.brigadeapp.data.sensors.LatLng
import com.example.brigadeapp.domain.sensors.LocationSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions


data class ProfileUiState(
    val name: String = "Mario",
    val available: Boolean = true,
    val userEmail: String? = null,
    val isOnCampus: Boolean? = null,
    val userPoint: LatLng? = null,
    val others: List<LatLng> = emptyList(),
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
    private val location: LocationSensorManager,
    private val appContext: Context,
    private val devFallbackEmail: String? = null,
    private val devMockLocation: LatLng? = null,
    private val campusCenter: LatLng = LatLng(4.6026783, -74.0653568),
    private val campusRadiusMeters: Double = 250.0
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val db by lazy { FirebaseFirestore.getInstance() }
    private var presenceListener: ListenerRegistration? = null


    init {
        // Firebase se acuerda de la sesion
        val initialEmail = auth.currentUser?.email ?: devFallbackEmail
        if (initialEmail != null) {
            _state.update { it.copy(userEmail = initialEmail) }
        }

        // Suscripcion a cambios de sesion
        viewModelScope.launch {
            auth.authState.collect { user ->
                val email = user?.email ?: devFallbackEmail
                _state.update { it.copy(userEmail = email) }
            }
        }

        observeOthersPresence()
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

    private fun upsertMyPresence(point: LatLng?, active: Boolean) {
        val email = auth.currentUser?.email ?: devFallbackEmail ?: return
        val data = hashMapOf(
            "email" to email,
            "active" to active,
            "updatedAt" to System.currentTimeMillis()
        ).apply {
            if (point != null) {
                put("lat", point.lat)
                put("lng", point.lng)
            }
        }
        db.collection("presence").document(email)
            .set(data, SetOptions.merge())
    }

    private fun getLocation() = viewModelScope.launch {
        if (!canReadLocation()) {
            _state.update { it.copy(error = "Missing location permission") }
            return@launch
        }
        _state.update { it.copy(isLoading = true, error = null) }

        val loc = location.getLastLocation()
        val point: LatLng? = when {
            loc != null             -> LatLng(loc.latitude, loc.longitude)
            devMockLocation != null -> devMockLocation
            else                    -> null
        }

        val onCampus = point?.let {
            location.isInsideRadius(
                point = it,
                center = campusCenter,
                radiusMeters = campusRadiusMeters
            )
        } ?: false


        upsertMyPresence(point, active = onCampus)

        _state.update { st ->
            val newCount = (if (onCampus) 1 else 0) + st.others.size
            st.copy(
                userPoint = point,
                isOnCampus = onCampus,
                insideCount = newCount,
                isLoading = false
            )
        }
    }


    private fun observeOthersPresence() {
        presenceListener?.remove()
        presenceListener = db.collection("presence")
            .whereEqualTo("active", true)
            .addSnapshotListener { snap, e ->
                if (e != null) return@addSnapshotListener
                val myEmail = auth.currentUser?.email ?: devFallbackEmail
                val points = snap?.documents?.mapNotNull { d ->
                    val email = d.getString("email") ?: return@mapNotNull null
                    if (email == myEmail) return@mapNotNull null
                    val lat = d.getDouble("lat") ?: return@mapNotNull null
                    val lng = d.getDouble("lng") ?: return@mapNotNull null
                    LatLng(lat, lng)
                }.orEmpty()

                // Filtra solo los que están dentro del radio del campus
                val insideOthers = points.filter {
                    location.isInsideRadius(it, campusCenter, campusRadiusMeters)
                }

                _state.update { st ->
                    val selfOnCampus = st.isOnCampus == true
                    st.copy(
                        others = insideOthers,
                        insideCount = (if (selfOnCampus) 1 else 0) + insideOthers.size
                    )
                }
            }
    }

    override fun onCleared() {
        presenceListener?.remove()
        presenceListener = null
        super.onCleared()
    }


}
