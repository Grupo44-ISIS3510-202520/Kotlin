package com.example.brigadeapp.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Emergency : Dest("emergency", "Emergency", Icons.Outlined.Alarm)
    data object Training  : Dest("training",  "Training",  Icons.Outlined.Book)
    data object Protocols : Dest("protocols", "Protocols", Icons.Outlined.Menu)
    data object Alerts    : Dest("alerts",    "Alerts",    Icons.Outlined.Notifications)
    data object Profile   : Dest("profile",   "Profile",   Icons.Outlined.AccountCircle)
}

val bottomItems = listOf(
    Dest.Emergency,
    Dest.Training,
    Dest.Protocols,
    Dest.Alerts,
    Dest.Profile
)
