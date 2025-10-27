package com.example.brigadeapp.view.screens

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.viewmodel.screens.RegisterViewModel

@Composable
fun RegisterRoute(
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val vm: RegisterViewModel = hiltViewModel()
    val state by vm.state.collectAsState()

    RegisterScreen(
        state = state,
        onEvent = vm::onEvent,
        onBack = onBack,
        onRegistered = onDone
    )
}
