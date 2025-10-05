package com.example.brigadeapp.nav


import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.brigadeapp.R

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.brigadeapp.alerts.AlertsScreen
import com.example.brigadeapp.home.HomeScreen
import com.example.brigadeapp.protocols.ProtocolsScreen
import com.example.brigadeapp.report.EmergencyReportScreen

import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.composable

private const val REPORT_ROUTE = "report"

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

@Composable
fun AppScaffold() {
    val nav = rememberNavController()

    Scaffold(
        bottomBar = {
            val entry by nav.currentBackStackEntryAsState()
            val currentDest = entry?.destination
            NavigationBar {
                bottomItems.forEach { d ->
                    NavigationBarItem(
                        selected = currentDest.isOn(d.route),
                        onClick = {
                            nav.navigate(d.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon  = { Icon(painterResource(d.iconRes), contentDescription = d.label) },
                        label = { Text(d.label) }
                    )
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Dest.Emergency.route,   // Home
            modifier = Modifier.padding(inner)
        ) {
            // HOME (menú principal)
            composable(Dest.Emergency.route) {
                HomeScreen(
                    onEmergencyClick = { nav.navigate(REPORT_ROUTE) },
                    onNotifications = { nav.navigate(Dest.Alerts.route){
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    }},
                    onProtocols = { nav.navigate(Dest.Protocols.route){
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    }},
                    onTraining = { nav.navigate(Dest.Training.route){
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    }},
                    onProfile = { nav.navigate(Dest.Profile.route){
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    }},
                    onCprGuide       = { /* nav.navigate("protocols/cpr") si lo crean */ }
                )
            }

            // Reporte (ruta ÚNICA)
            composable(REPORT_ROUTE) {
                EmergencyReportScreen(
                    onBack = { nav.popBackStack() },
                    onSubmit = { /* TODO */ }
                )
            }

            // TRAINING
            composable(Dest.Training.route) {
                com.example.brigadeapp.training.ui.TrainingScreen()
            }

            // PROTOCOLS
            composable(Dest.Protocols.route) {
                ProtocolsScreen(onBack = { nav.popBackStack() })
            }

            // ALERTS
            composable(Dest.Alerts.route) {
                AlertsScreen()
            }

            // PROFILE
            composable(Dest.Profile.route) {
                // com.example.brigadeapp.profile.ui.ProfileScreen()
                val ctx = androidx.compose.ui.platform.LocalContext.current

                //  Usa fake auth (sin Firebase)
                val auth = remember {
                    com.example.brigadeapp.core.auth.AuthClientFake()
                }

                //   Mock de ubicación cerca del campus (puedes moverla para probar on/off campus)
                //   Ejemplo: justo en el centro para ver "On campus"
                val mockLatLng = remember {
                    com.example.brigadeapp.core.location.LatLng(4.6026783, -74.0653568)
                    // Para probar "Off campus", usar algo más lejano como 4.60, -74.07
                    // com.example.brigadeapp.core.location.LatLng(4.6000, -74.0700)
                }

                val vm = remember {
                    com.example.brigadeapp.profile.vm.ProfileViewModel(
                        auth = auth,
                        location = com.example.brigadeapp.core.location.FusedLocationClient(ctx),
                        appContext = ctx.applicationContext,
                        devFallbackEmail = "dev@mock.local",  // correo ficticio visible en UI
                        devMockLocation = mockLatLng         // mock de ubicación si lastLocation == null
                    )
                }
                val state by vm.state.collectAsState()

                // Revisar que se esta usando stateless
                com.example.brigadeapp.profile.ui.ProfileScreen(
                    state = state,
                    onEvent = vm::onEvent
                )
            }
        }
    }
}

// Marca el tab correcto aunque naveguemos a subrutas
private fun NavDestination?.isOn(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true