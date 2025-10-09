@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.brigadeapp.presentation.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.presentation.viewmodel.ProfileUiEvent
import com.example.brigadeapp.presentation.viewmodel.ProfileUiState
import com.example.brigadeapp.ui.common.StandardScreen
import com.example.brigadeapp.ui.theme.*

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    onBack: (() -> Unit)? = null
) {
    // Launcher para permisos de ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val ok = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) onEvent(ProfileUiEvent.RequestLocation)
    }

    StandardScreen(title = "Brigadist Profile", onBack = onBack) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(SurfaceSoft)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // Disponibilidad + acción
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvailabilityChip(available = state.available)
                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = { onEvent(ProfileUiEvent.ToggleAvailability) },
                    label = { Text(if (state.available) "Set unavailable" else "Set available") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Avatar
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

            // Campos (stateless). Por ahora read-only hasta que agregues eventos de edición.
            LabeledField("Name:", state.name, onValueChange = null, placeholder = "Your name", enabled = false)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                LabeledField("Blood type:", "", onValueChange = null, placeholder = "A, B, AB, O", enabled = false, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                LabeledField("RH:", "", onValueChange = null, placeholder = "+ / -", enabled = false, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            LabeledField("Time availability:", "", onValueChange = null, placeholder = "Time slots", enabled = false)

            Spacer(Modifier.height(16.dp))

            // Estado de ubicación + botón para pedir permisos y verificar
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

            // Error ligero (si lo hay)
            state.error?.let {
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = { onEvent(ProfileUiEvent.ClearError) },
                    label = { Text(it) },
                )
            }

            Spacer(Modifier.height(16.dp))

            // Rewards (muestra)
            Text("REWARDS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            Divider(Modifier.padding(vertical = 6.dp), color = GreyOutline)
            listOf("Medal 1", "Medal 2", "Medal 3", "Medal 4").forEach {
                RewardItem(title = it, onClick = { /* TODO */ })
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Sign out si hay usuario
            if (state.userEmail != null) {
                OutlinedButton(
                    onClick = { onEvent(ProfileUiEvent.SignOut) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Sign out (${state.userEmail})")
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

/* ------------------------- Sub-composables ------------------------- */

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

@Composable
private fun LocationSection(
    isOnCampus: Boolean?,
    isLoading: Boolean,
    onCheckLocation: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val label = when (isOnCampus) {
                true -> "On campus ✅"
                false -> "Off campus ❌"
                null -> "Location not checked"
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
