@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.brigadeapp.view.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.brigadeapp.data.sensors.CAMPUS_LAT
import com.example.brigadeapp.data.sensors.CAMPUS_LNG
import com.example.brigadeapp.data.sensors.CAMPUS_RADIUS_METERS
import com.example.brigadeapp.data.sensors.LatLng
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.view.theme.BrigadeAppTheme
import com.example.brigadeapp.view.theme.GreyOutline
import com.example.brigadeapp.view.theme.Red
import com.example.brigadeapp.view.theme.Success
import com.example.brigadeapp.view.theme.SurfaceSoft
import com.example.brigadeapp.viewmodel.screens.ProfileUiEvent
import com.example.brigadeapp.viewmodel.screens.ProfileUiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng as GmsLatLng

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    onBack: (() -> Unit)? = null,
    isOnline: Boolean = true
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) onEvent(ProfileUiEvent.RequestLocation)
    }

    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) onEvent(ProfileUiEvent.RequestLocation)
    }

    StandardScreen(title = "Brigadist Profile", onBack = onBack) { inner ->

        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(scroll)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AvailabilityChip(available = state.available)
                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = { onEvent(ProfileUiEvent.ToggleAvailability) },
                    label = { Text(if (state.available) "Set unavailable" else "Set available") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // TODO: Avatar real
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            LabeledField("Name:", state.name, onValueChange = null, placeholder = "Your name", enabled = false)
            Spacer(Modifier.height(8.dp))

            val abo = remember(state.bloodGroup) {
                state.bloodGroup.takeWhile { it.isLetter() }.ifBlank { "" }
            }
            val rh = remember(state.bloodGroup) {
                state.bloodGroup.takeLast(1).takeIf { it == "+" || it == "-" } ?: ""
            }

            Row(Modifier.fillMaxWidth()) {
                LabeledField(
                    label = "Blood type:",
                    value = abo,
                    onValueChange = null,
                    placeholder = "A, B, AB, O",
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                LabeledField(
                    label = "RH:",
                    value = rh,
                    onValueChange = null,
                    placeholder = "+ / -",
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            LabeledField(
                label = "Role:",
                value = state.role,
                onValueChange = null,
                placeholder = "Role",
                enabled = false
            )

            Spacer(Modifier.height(8.dp))

            LabeledField(
                label = "Uniandes Code:",
                value = state.uniandesCode,
                onValueChange = null,
                placeholder = "Code",
                enabled = false
            )

            Spacer(Modifier.height(16.dp))

            LocationSection(
                isOnCampus = state.isOnCampus,
                isLoading = state.isLoading,
                onCheckLocation = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            Spacer(Modifier.height(16.dp))

            // Mapa u offline placeholder
            if (!isOnline) {
                MapOfflinePlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                GoogleCampusMap(
                    user = state.userPoint,
                    campus = LatLng(CAMPUS_LAT, CAMPUS_LNG),
                    radiusMeters = CAMPUS_RADIUS_METERS,
                    others = state.others,       // <- otros brigadistas
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Brigadists on campus (radius ${CAMPUS_RADIUS_METERS.toInt()}m): ${state.insideCount}",
                style = MaterialTheme.typography.bodyLarge
            )

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                AssistChip(onClick = { onEvent(ProfileUiEvent.ClearError) }, label = { Text(it) })
            }

            Spacer(Modifier.height(16.dp))

            Text("REWARDS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            Divider(Modifier.padding(vertical = 6.dp), color = GreyOutline)
            listOf("Medal 1", "Medal 2", "Medal 3", "Medal 4").forEach {
                RewardItem(title = it, onClick = { })
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            if (state.userEmail != null) {
                OutlinedButton(onClick = { onEvent(ProfileUiEvent.SignOut) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Log out")
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun AvailabilityChip(available: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background((if (available) Success else Red).copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (available) Success else Red)
        )
        Spacer(Modifier.width(8.dp))
        Text(if (available) "Available now" else "Unavailable", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun LocationSection(isOnCampus: Boolean?, isLoading: Boolean, onCheckLocation: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val label = when (isOnCampus) {
            true -> "On campus"
            false -> "Off campus"
            null -> "Location not checked"
        }
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(12.dp))
        Button(onClick = onCheckLocation, enabled = !isLoading, shape = RoundedCornerShape(20.dp)) {
            Text(if (isLoading) "Checking…" else "Check location")
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: ((String) -> Unit)?,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { v -> onValueChange?.invoke(v) ?: Unit },
            enabled = enabled && onValueChange != null,
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RewardItem(title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Star, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun GoogleCampusMap(
    user: LatLng?,
    campus: LatLng,
    radiusMeters: Double,
    others: List<LatLng>,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val campusGms = remember(campus) { GmsLatLng(campus.lat, campus.lng) }
    val userGms = user?.let { GmsLatLng(it.lat, it.lng) }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(campusGms, 16f)
    }

    LaunchedEffect(userGms) {
        val target = userGms ?: campusGms
        val zoom = if (userGms != null) 17f else 16f
        cameraState.animate(CameraUpdateFactory.newLatLngZoom(target, zoom))
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = true,
            compassEnabled = true,
            zoomControlsEnabled = false
        )
    ) {
        // Círculo del campus
        Circle(
            center = campusGms,
            radius = radiusMeters,
            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            strokeColor = MaterialTheme.colorScheme.primary,
            strokeWidth = 2f
        )

        // Usuario (rojo)
        if (userGms != null) {
            Marker(
                state = MarkerState(position = userGms),
                title = "You",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        // Otros (azul)
        others.forEach { p ->
            val pos = GmsLatLng(p.lat, p.lng)
            Marker(
                state = MarkerState(position = pos),
                title = "Brigadist",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }
    }
}

@Composable
private fun MapOfflinePlaceholder(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.CloudOff, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text("Map not available (offline)", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("Reconnect to see the campus and live locations.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}
