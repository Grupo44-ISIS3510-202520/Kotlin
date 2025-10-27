package com.example.brigadeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.example.brigadeapp.domain.entity.FirebaseAuthClient
import com.example.brigadeapp.view.components.AppScaffold
import com.example.brigadeapp.view.screens.AuthNavHost // NEW
import com.example.brigadeapp.view.theme.BrigadeAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BrigadeAppTheme {
                val auth = remember { FirebaseAuthClient() }
                val user = auth.authState.collectAsState(initial = auth.currentUser).value

                if (user == null) {
                    AuthNavHost(                      // NEW
                        onAuthCompleted = { /* no-op */ } // NEW
                    )                                  // NEW
                } else {
                    AppScaffold(auth = auth)
                }
            }
        }
    }
}
