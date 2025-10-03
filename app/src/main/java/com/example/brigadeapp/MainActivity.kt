package com.example.brigadeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.brigadeapp.nav.AppScaffold
import com.example.brigadeapp.ui.theme.BrigadeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrigadeAppTheme {
                AppScaffold()
            }
        }
    }
}
