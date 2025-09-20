package com.example.brigadeapp.nav


import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.brigadeapp.R   // acceso a tus drawables


sealed class Dest(val route: String, val label: String, val iconRes: Int) {
    data object Emergency : Dest("emergency", "Emergency", R.drawable.ic_emergency)
    data object Training  : Dest("training",  "Training",  R.drawable.ic_training)
    data object Protocols : Dest("protocols", "Protocols", R.drawable.ic_protocols)
    data object Alerts    : Dest("alerts",    "Alerts",    R.drawable.ic_alert)
    data object Profile   : Dest("profile",   "Profile",   R.drawable.ic_profile)
}
val bottomItems = listOf(
    Dest.Emergency,
    Dest.Training,
    Dest.Protocols,
    Dest.Alerts,
    Dest.Profile
)
