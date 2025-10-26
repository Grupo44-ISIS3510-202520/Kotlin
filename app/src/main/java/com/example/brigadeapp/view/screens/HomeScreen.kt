package com.example.brigadeapp.view.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.core.AuthClient
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.view.components.CallButton
import com.example.brigadeapp.viewmodel.screens.HomeViewModel
import com.example.brigadeapp.viewmodel.utils.ConnectivityViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    auth: AuthClient,
    onEmergencyClick: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onProtocols: () -> Unit = {},
    onTraining: () -> Unit = {},
    onProfile: () -> Unit = {},
    onCprGuide: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    connectivityViewModel: ConnectivityViewModel = hiltViewModel()
) {
    val isInsideCampusState = viewModel.isInsideCampus.collectAsState()
    val isInsideCampus = isInsideCampusState.value

    val isOnlineState = connectivityViewModel.isOnline.collectAsState()
    val isOnline = isOnlineState.value

    StandardScreen(title = "Emergency Dashboard") { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón grande de emergencia
            val numberToCall = if (isInsideCampus || !isOnline){ "6013394949" } else { "123" }
            CallButton(number = numberToCall)

            // Grid de accesos rápidos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { DashboardCard("Notifications", Icons.Outlined.Notifications, onNotifications) }
                item { DashboardCard("Protocols", Icons.Outlined.Menu, onProtocols) }
                item { DashboardCard("Training", Icons.Outlined.CheckCircle, onTraining) }
                item { DashboardCard("Profile", Icons.Outlined.AccountCircle, onProfile) }
            }

            Spacer(Modifier.height(10.dp))

            // CPR Guide
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable(onClick = onCprGuide),
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.FavoriteBorder, null)
                        Spacer(Modifier.width(8.dp))
                        Text("CPR Guide", style = MaterialTheme.typography.bodyLarge)
                    }

                    Button(
                        onClick = onEmergencyClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(80.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    MaterialTheme {
        HomeScreen(
            auth = TODO()
        )
    }
}
