package com.example.brigadeapp.view.screens

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brigadeapp.viewmodel.screens.SignInViewModel

@Composable
fun AuthNavHost(
    onAuthCompleted: () -> Unit = {}
) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "signin") {
        composable("signin") {
            val vm: SignInViewModel = hiltViewModel()
            val state by vm.state.collectAsState()
            SignInScreen(
                state = state,
                onEvent = vm::onEvent,
                onCreateAccount = { nav.navigate("register") } // NEW
            )
        }
        composable("register") {
            RegisterRoute(
                onBack = { nav.popBackStack() },
                onDone = {
                    // Se vuelve a SignIn tras mostrar el diálogo OK
                    nav.popBackStack("signin", inclusive = false)
                }
            )
        }
    }
}
