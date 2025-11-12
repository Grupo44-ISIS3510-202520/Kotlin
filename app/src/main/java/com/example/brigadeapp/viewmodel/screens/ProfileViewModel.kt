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
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

// NEW: for the heartbeat loop
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class ProfileUiState(
    val name: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val bloodGroup: String = "",
    val role: String = "",
    val uniandesCode: String = "",

    val available: Boolean = true,
    val userEmail: String? = null,
    val isOnCampus: Boolean? = null,
    val userPoint: com.example.brigadeapp.data.sensors.LatLng? = null,
    val others: List<com.example.brigadeapp.data.sensors.LatLng> = emptyList(),
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

    private val db by lazy { FirebaseFirestore.getInstance() }

    private var userDocUnsub: ListenerRegistration? = null

    private var presenceJob: Job? = null

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {

        val initialEmail = auth.currentUser?.email ?: devFallbackEmail
        if (initialEmail != null) {
            _state.update { it.copy(userEmail = initialEmail) }
        }

        val initialUser = auth.currentUser
        _state.update { it.copy(userEmail = initialUser?.email) }
        startUserProfileListener(initialUser?.uid)

        viewModelScope.launch {
            auth.authState.collect { user ->
                _state.update { it.copy(userEmail = user?.email) }
                startUserProfileListener(user?.uid)
            }
        }

        """
        viewModelScope.launch {
            auth.authState.collect { user ->
                val email = user?.email ?: devFallbackEmail
                _state.update { it.copy(userEmail = email) }
            }
        }
        """

        startPresenceListener()

        startPresenceHeartbeat()

        loadUserName()

    }

    fun onEvent(e: ProfileUiEvent) {
        when (e) {
            ProfileUiEvent.ToggleAvailability -> {
                _state.update { it.copy(available = !it.available) }
                val p = state.value.userPoint
                viewModelScope.launch {
                    if (p != null) updatePresence(p, state.value.isOnCampus == true)
                }
            }
            ProfileUiEvent.SignOut         -> auth.signOut()
            ProfileUiEvent.RequestLocation -> getLocationAndPersist()
            ProfileUiEvent.ClearError      -> _state.update { it.copy(error = null) }
        }
    }

    private fun hasFineLocation(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocation(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun canReadLocation() = hasFineLocation() || hasCoarseLocation()

    private fun getLocationAndPersist() = viewModelScope.launch {
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
            location.isInsideRadius(point = it, center = campusCenter, radiusMeters = campusRadiusMeters)
        } ?: false

        _state.update {
            it.copy(
                userPoint = point,
                isOnCampus = onCampus,
                isLoading = false
            )
        }

        if (point != null) {
            updatePresence(point, onCampus)
        }
    }

    private suspend fun getLocationAndPersistSilently() {
        if (!canReadLocation()) return

        val loc = location.getLastLocation()
        val point: LatLng? = when {
            loc != null             -> LatLng(loc.latitude, loc.longitude)
            devMockLocation != null -> devMockLocation
            else                    -> null
        }

        val onCampus = point?.let {
            location.isInsideRadius(point = it, center = campusCenter, radiusMeters = campusRadiusMeters)
        } ?: false

        _state.update { it.copy(userPoint = point, isOnCampus = onCampus) }

        if (point != null) updatePresence(point, onCampus)
    }

    private fun startPresenceHeartbeat(periodMs: Long = 30_000L) {
        presenceJob?.cancel()
        presenceJob = viewModelScope.launch {
            while (isActive) {
                try { getLocationAndPersistSilently() } catch (_: Throwable) {}
                delay(periodMs)
            }
        }
    }

    private suspend fun updatePresence(point: LatLng, onCampus: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: devFallbackEmail

        val data = hashMapOf(
            "lat" to point.lat,
            "lng" to point.lng,
            "email" to email,
            "available" to state.value.available,
            "onCampus" to onCampus,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("presence").document(uid)
            .set(data, SetOptions.merge())
            .await()
    }

    private fun startPresenceListener() {
        val myUid = auth.currentUser?.uid
        val staleLimitMs = 10 * 60 * 1000L

        db.collection("presence")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener

                val now = System.currentTimeMillis()
                val others = snap.documents.mapNotNull { doc ->
                    if (doc.id == myUid) return@mapNotNull null

                    val lat = doc.getDouble("lat") ?: return@mapNotNull null
                    val lng = doc.getDouble("lng") ?: return@mapNotNull null
                    val on  = doc.getBoolean("onCampus") ?: false

                    val updatedMs = millisFrom(doc.get("updatedAt"))

                    if (updatedMs != null && (now - updatedMs > staleLimitMs)) return@mapNotNull null

                    val p = LatLng(lat, lng)
                    if (!on || !location.isInsideRadius(p, campusCenter, campusRadiusMeters)) return@mapNotNull null
                    p
                }

                _state.update { cur ->
                    val meInside = cur.isOnCampus == true
                    cur.copy(
                        others = others,
                        insideCount = others.size + if (meInside) 1 else 0
                    )
                }
            }
    }

    private fun millisFrom(value: Any?): Long? = when (value) {
        is com.google.firebase.Timestamp -> value.toDate().time
        is java.util.Date               -> value.time
        is Number                       -> value.toLong()
        is String                       -> value.toLongOrNull()
        else                            -> null
    }

    private fun startUserProfileListener(uid: String?) {
        userDocUnsub?.remove()
        if (uid == null) return

        userDocUnsub = db.collection("users").document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null || !snap.exists()) return@addSnapshotListener

                val first = snap.getString("name") ?: ""
                val last  = snap.getString("lastName") ?: ""
                val bg    = snap.getString("bloodGroup") ?: ""
                val role  = snap.getString("role") ?: ""
                val code  = snap.getString("uniandesCode") ?: (snap.getString("code") ?: "")

                _state.update {
                    it.copy(
                        firstName = first,
                        lastName = last,
                        name = listOf(first, last).filter { s -> s.isNotBlank() }.joinToString(" "),
                        bloodGroup = bg,
                        role = role,
                        uniandesCode = code
                    )
                }
            }
    }

    override fun onCleared() {
        presenceJob?.cancel()
        super.onCleared()
    }

    private fun loadUserName() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            val name = (doc.getString("name") ?: "").trim()
            val last = (doc.getString("lastName") ?: "").trim()
            val full = listOf(name, last).filter { it.isNotBlank() }.joinToString(" ")
            if (full.isNotBlank()) _state.update { it.copy(name = full) }
        } catch (_: Throwable) { /* no-op */ }
    }

    

}
