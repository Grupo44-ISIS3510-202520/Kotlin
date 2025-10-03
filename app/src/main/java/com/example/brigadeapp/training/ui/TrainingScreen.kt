package com.example.brigadeapp.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.ui.theme.Blue
import com.example.brigadeapp.ui.theme.SurfaceSoft

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.brigadeapp.R



import androidx.compose.ui.tooling.preview.Preview
import com.example.brigadeapp.ui.theme.BrigadeAppTheme

@Preview(
    name = "Training – Light",
    showBackground = true,
    backgroundColor = 0xFFF4F7FB,
    widthDp = 360, heightDp = 800
)
@Composable
fun TrainingScreenPreview() {
    BrigadeAppTheme { TrainingScreen() }
}

@Preview(
    name = "Training – Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 360, heightDp = 800
)
@Composable
fun TrainingScreenPreviewDark() {
    BrigadeAppTheme { TrainingScreen() }
}


@Composable
fun TrainingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceSoft
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* back */ }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text("Training", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = { /* menu */ }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("First Aid", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))

            Spacer(Modifier.height(8.dp))
            TrainingCard(
                badge = "Course",
                title = "Basic First Aid",
                subtitle = "Learn the essentials of first aid, including CPR and basic wound care.",
                cta = "Start Course",
                imageRes = R.drawable.basic_first_aid,
                onClick = { /* TODO */ }
            )
            Spacer(Modifier.height(12.dp))
            TrainingCard(
                badge = "Certification",
                title = "Advanced First Aid",
                subtitle = "Get certified in advanced first aid techniques and emergency response.",
                cta = "Start Certification",
                imageRes = R.drawable.advanced_first_aid,
                onClick = { /* TODO */ }
            )

            Spacer(Modifier.height(18.dp))
            Text("Your Progress", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            Spacer(Modifier.height(8.dp))
            ProgressItem(label = "First Aid Course", progress = 0.75f)
            Spacer(Modifier.height(10.dp))
            ProgressItem(label = "Advanced Certification", progress = 0.25f)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TrainingCard(
    badge: String,
    title: String,
    subtitle: String,
    cta: String,
    @DrawableRes imageRes: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen importada desde drawable
            Image(
                painter = painterResource(imageRes),
                contentDescription = null, // decorativa; usa texto si necesitas accesibilidad
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceSoft)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // Pequeño badge arriba (Course / Certification)
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) { Text(cta) }
            }
        }
    }
}

@Composable
private fun ProgressItem(label: String, progress: Float) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                trackColor = SurfaceSoft,
                color = Blue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}
