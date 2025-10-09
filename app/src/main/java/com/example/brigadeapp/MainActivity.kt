package com.example.brigadeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.brigadeapp.presentation.ui.AppScaffold
import com.example.brigadeapp.ui.theme.BrigadeAppTheme
import dagger.hilt.android.AndroidEntryPoint

// Auth (real y fake)
import com.example.brigadeapp.core.auth.AuthClient
import com.example.brigadeapp.core.auth.FirebaseAuthClient
import com.example.brigadeapp.core.auth.AuthClientFake

// Login UI + VM
import com.example.brigadeapp.auth.SignInScreen
import com.example.brigadeapp.auth.SignInViewModel

// App (bottom bar + NavHost)

import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.google.firebase.FirebaseApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            BrigadeAppTheme {
                AppEntry(useFirebase = true)
            }
        }
    }
}

@Composable
private fun AppEntry(useFirebase: Boolean) {
    // Proveedor de Auth con fallback
    val auth: AuthClient = remember {
        if (useFirebase) FirebaseAuthClient() else AuthClientFake()
    }

    // Observador de la sesión (Firebase recuerda al usuario entre aperturas)
    val user by auth.authState.collectAsState(initial = auth.currentUser)

    if (user == null) {
        val vm = remember { SignInViewModel(auth) }
        val st by vm.state.collectAsState()
        SignInScreen(
            state = st,
            onEvent = vm::onEvent,
            // logoRes = R.drawable.app_logo
        )
    } else {
        AppScaffold()
    }
}
