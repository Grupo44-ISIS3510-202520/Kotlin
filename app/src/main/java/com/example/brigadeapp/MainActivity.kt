package com.example.brigadeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.example.brigadeapp.core.auth.FirebaseAuthClient
import com.example.brigadeapp.auth.SignInScreen
import com.example.brigadeapp.auth.SignInViewModel
import com.example.brigadeapp.presentation.ui.AppScaffold
import com.example.brigadeapp.ui.theme.BrigadeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BrigadeAppTheme {
                val auth = remember { FirebaseAuthClient() }

                // Firebase recordara la sesion
                val user = auth.authState.collectAsState(initial = auth.currentUser).value

                if (user == null) {
                    // Pantalla de login
                    val vm = remember { SignInViewModel(auth) }
                    val state = vm.state.collectAsState().value
                    SignInScreen(state = state, onEvent = vm::onEvent)
                } else {
                    // App recibiendo instancia de autenticacion
                    AppScaffold(auth = auth)
                }
            }
        }
    }
}
