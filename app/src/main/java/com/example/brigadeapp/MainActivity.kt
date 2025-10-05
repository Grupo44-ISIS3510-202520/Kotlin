package com.example.brigadeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.example.brigadeapp.core.auth.FirebaseAuthClient
import com.example.brigadeapp.auth.SignInScreen
import com.example.brigadeapp.auth.SignInViewModel
import com.example.brigadeapp.nav.AppScaffold
import com.example.brigadeapp.ui.theme.BrigadeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BrigadeAppTheme {
                // ÚNICA instancia compartida de Auth
                val auth = remember { FirebaseAuthClient() }

                // “Gate” de autenticación (Firebase recuerda sesión)
                val user = auth.authState.collectAsState(initial = auth.currentUser).value

                if (user == null) {
                    // Pantalla de login usando la MISMA instancia de auth
                    val vm = remember { SignInViewModel(auth) }
                    val state = vm.state.collectAsState().value
                    SignInScreen(state = state, onEvent = vm::onEvent)
                } else {
                    // App principal (BottomBar + NavHost), recibiendo la MISMA instancia
                    AppScaffold(auth = auth)
                }
            }
        }
    }
}
