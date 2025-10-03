package com.example.brigadeapp.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.ui.common.StandardScreen
import com.example.brigadeapp.ui.theme.*

@Composable
fun ProfileScreen(
    onBack: () -> Unit = {} // por si la necesitas en otro flujo
) {
    var name by remember { mutableStateOf("Mario") }
    var bloodType by remember { mutableStateOf("") }
    var rh by remember { mutableStateOf("") }
    var available by remember { mutableStateOf(true) }

    StandardScreen(title = "Brigadist Profile", onBack = null) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)               // <- respeta top bar
                .fillMaxSize()
                .background(SurfaceSoft)      // fondo pastel definido en tema
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // Chip de disponibilidad
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background((if (available) Success else Red).copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = { available = !available },
                    label = { Text(if (available) "Set unavailable" else "Set available") }
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

            // Campos
            LabeledField("Name:", name, { name = it }, "Your name")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                LabeledField("Blood type:", bloodType, { bloodType = it }, "A, B, AB, O", Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                LabeledField("RH:", rh, { rh = it }, "+ / -", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            LabeledField("Time availability:", "", {}, "Time slots", enabled = false)

            Spacer(Modifier.height(16.dp))

            // Rewards
            Text("REWARDS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            Divider(Modifier.padding(vertical = 6.dp), color = GreyOutline)

            listOf("Medal 1", "Medal 2", "Medal 3", "Medal 4").forEach {
                RewardItem(title = it, onClick = { /* TODO */ })
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
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

@Preview(
    name = "Profile – Light",
    showBackground = true,
    backgroundColor = 0xFFF4F7FB,
    widthDp = 360, heightDp = 800
)
@Composable
private fun ProfileScreenPreview() {
    com.example.brigadeapp.ui.theme.BrigadeAppTheme {
        ProfileScreen()
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
    com.example.brigadeapp.ui.theme.BrigadeAppTheme {
        ProfileScreen()
    }
}
