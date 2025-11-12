package com.example.brigadeapp.view.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.brigadeapp.data.sensors.LocationSensorImpl
import com.example.brigadeapp.viewmodel.screens.ProfileViewModel
import com.example.brigadeapp.view.screens.ProfileScreen

// IMPORTA o ajusta el paquete donde dejaste tu AuthClient real
import com.example.brigadeapp.domain.entity.FirebaseAuthClient

@Composable
fun ProfileRoute() {
    val ctx = LocalContext.current
    val vm = remember {
        ProfileViewModel(
            auth = FirebaseAuthClient(),
            location = LocationSensorImpl(ctx),
            appContext = ctx.applicationContext,
            devFallbackEmail = null,
            devMockLocation = null
        )
    }
    val state by vm.state.collectAsState()
    ProfileScreen(state = state, onEvent = vm::onEvent)
}
