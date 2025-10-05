@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.brigadeapp.profile.ui

// ------------------------ Imports (solo al inicio del archivo) ------------------------
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.brigadeapp.core.location.CAMPUS_LAT
import com.example.brigadeapp.core.location.CAMPUS_LNG
import com.example.brigadeapp.core.location.CAMPUS_RADIUS_METERS
import com.example.brigadeapp.core.location.LatLng
import com.example.brigadeapp.profile.vm.ProfileUiEvent
import com.example.brigadeapp.profile.vm.ProfileUiState
import com.example.brigadeapp.ui.common.StandardScreen
import com.example.brigadeapp.ui.theme.*   // Tema del proyecto
import kotlin.math.PI
import kotlin.math.cos
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

// -------------------------------------------------------------------------------------

/**
 * Pantalla de perfil (stateless).
 * - El estado se recibe por parámetro.
 * - Las acciones del usuario se emiten vía `onEvent`.
 * - Se incluye solicitud de permisos de ubicación, mapa con círculo del campus,
 *   y botón de cerrar sesión (si existe un correo en el estado).
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    onBack: (() -> Unit)? = null
) {
    // El launcher gestiona la solicitud de múltiples permisos de ubicación.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) onEvent(ProfileUiEvent.RequestLocation)
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
                .background(SurfaceSoft)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // Se muestra el chip de disponibilidad junto con una acción para alternar.
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvailabilityChip(available = state.available)
                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = { onEvent(ProfileUiEvent.ToggleAvailability) },
                    label = { Text(if (state.available) "Set unavailable" else "Set available") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Avatar decorativo.
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

            // Campos de solo lectura por el momento.
            LabeledField("Name:", state.name, onValueChange = null, placeholder = "Your name", enabled = false)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                LabeledField(
                    label = "Blood type:",
                    value = "",
                    onValueChange = null,
                    placeholder = "A, B, AB, O",
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                LabeledField(
                    label = "RH:",
                    value = "",
                    onValueChange = null,
                    placeholder = "+ / -",
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            LabeledField("Time availability:", "", onValueChange = null, placeholder = "Time slots", enabled = false)

            Spacer(Modifier.height(16.dp))

            // Sección de ubicación: indicador + botón que solicita permisos y consulta ubicación.
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

            // Mapa simple con el círculo del campus y el punto del usuario si está disponible.
            Spacer(Modifier.height(16.dp))
            CampusMap(
                user = state.userPoint,
                campus = LatLng(CAMPUS_LAT, CAMPUS_LNG),
                radiusMeters = CAMPUS_RADIUS_METERS,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
            )

            // Contador de personas dentro del radio (en esta iteración: 1 si el usuario está dentro; 0 si no).
            Spacer(Modifier.height(12.dp))
            Text(
                text = "People on campus (radius ${CAMPUS_RADIUS_METERS.toInt()}m): ${state.insideCount}",
                style = MaterialTheme.typography.bodyLarge
            )

            // Si hubiera un error ligero, se mostraría en un AssistChip para poder limpiarlo.
            state.error?.let {
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = { onEvent(ProfileUiEvent.ClearError) },
                    label = { Text(it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Sección de recompensas (demostrativa).
            Text("REWARDS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            Divider(Modifier.padding(vertical = 6.dp), color = GreyOutline)
            listOf("Medal 1", "Medal 2", "Medal 3", "Medal 4").forEach {
                RewardItem(title = it, onClick = { /* Pending */ })
                Spacer(Modifier.height(8.dp))
            }

            // Botón de cerrar sesión, visible únicamente si existe correo en el estado.
            Spacer(Modifier.height(16.dp))
            if (state.userEmail != null) {
                OutlinedButton(
                    onClick = { onEvent(ProfileUiEvent.SignOut) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    //Text("Log out (${state.userEmail})")
                    Text("Log out")
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

/* ------------------------- Sub-composables ------------------------- */

/** Chip de disponibilidad con color y etiqueta. */
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
        Text(
            if (available) "Available now" else "Unavailable",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** Sección de estado de ubicación con botón para solicitar permisos y consultar. */
@Composable
private fun LocationSection(
    isOnCampus: Boolean?,
    isLoading: Boolean,
    onCheckLocation: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val label = when (isOnCampus) {
            true  -> "On campus ✅"
            false -> "Off campus ❌"
            null  -> "Location not checked"
        }
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(12.dp))
        Button(
            onClick = onCheckLocation,
            enabled = !isLoading,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(if (isLoading) "Checking…" else "Check location")
        }
    }
}

/** Campo etiquetado. Si `onValueChange == null`, el campo se mostrará deshabilitado. */
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

/** Ítem de recompensa (demostrativo). */
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
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Star, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/**
 * Mapa minimalista: se dibuja el círculo del campus y el punto del usuario, si se dispone de él.
 * La escala se aproxima para que el círculo quepa con margen horizontal.
 */
@Composable
private fun CampusMap(
    user: LatLng?,
    campus: LatLng,
    radiusMeters: Double,
    modifier: Modifier = Modifier
) {
    // Capturar colores en contexto @Composable (aquí sí se puede)
    val primary = MaterialTheme.colorScheme.primary
    val primaryFaint = primary.copy(alpha = 0.15f)
    val tertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)

        // Escala aproximada: ~6 radios dentro del ancho
        val pxPerMeter: Float = size.width / (6f * radiusMeters.toFloat())

        // Círculo del campus (usar Float en el radio)
        drawCircle(
            color = primaryFaint,
            radius = radiusMeters.toFloat() * pxPerMeter,
            center = center
        )

        // Punto del campus
        drawCircle(
            color = primary,
            radius = 6.dp.toPx(),
            center = center
        )

        // Punto del usuario (si existe)
        user?.let { u ->
            val metersPerDegLat = 111_320.0
            val metersPerDegLon = 111_320.0 * cos(campus.lat * PI / 180.0)
            val dxMeters = ((u.lng - campus.lng) * metersPerDegLon).toFloat()
            val dyMeters = ((u.lat - campus.lat) * metersPerDegLat).toFloat()
            val userOffset = center + Offset(dxMeters * pxPerMeter, -dyMeters * pxPerMeter)

            drawCircle(
                color = tertiary,
                radius = 6.dp.toPx(),
                center = userOffset
            )
        }
    }
}


/* ------------------------- Previews ------------------------- */

@Preview(
    name = "Profile – Light",
    showBackground = true,
    backgroundColor = 0xFFF4F7FB,
    widthDp = 360, heightDp = 800
)
@Composable
private fun ProfileScreenPreview() {
    BrigadeAppTheme {
        ProfileScreen(
            state = ProfileUiState(
                name = "Mario",
                available = true,
                userEmail = "mario@uniandes.edu.co",
                isOnCampus = true,
                isLoading = false
            ),
            onEvent = {}
        )
    }
}

@Preview(
    name = "Profile – Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360, heightDp = 800
)
@Composable
private fun ProfileScreenPreviewDark() {
    BrigadeAppTheme {
        ProfileScreen(
            state = ProfileUiState(
                name = "Mario",
                available = false,
                userEmail = null,
                isOnCampus = null,
                isLoading = false
            ),
            onEvent = {}
        )
    }
}
