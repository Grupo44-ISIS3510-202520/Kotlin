package com.example.brigadeapp.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.brigadeapp.R
import com.example.brigadeapp.alerts.AlertsScreen
import com.example.brigadeapp.core.auth.AuthClient
import com.example.brigadeapp.core.location.FusedLocationClient
import com.example.brigadeapp.core.location.LatLng
import com.example.brigadeapp.home.HomeScreen
import com.example.brigadeapp.profile.ui.ProfileScreen
import com.example.brigadeapp.profile.vm.ProfileViewModel
import com.example.brigadeapp.protocols.ProtocolsScreen
import com.example.brigadeapp.report.EmergencyReportScreen
import com.example.brigadeapp.training.ui.TrainingScreen

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

/** Ahora recibe AuthClient para NO crear otra instancia en esta capa. */
@Composable
fun AppScaffold(auth: AuthClient) {
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
                        icon = { Icon(painterResource(d.iconRes), contentDescription = d.label) },
                        label = { Text(d.label) }
                    )
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Dest.Emergency.route,
            modifier = Modifier.padding(inner)
        ) {
            // HOME
            composable(Dest.Emergency.route) {
                HomeScreen(
                    onEmergencyClick = { nav.navigate(REPORT_ROUTE) },
                    onNotifications  = { nav.navigate(Dest.Alerts.route) },
                    onProtocols      = { nav.navigate(Dest.Protocols.route) },
                    onTraining       = { nav.navigate(Dest.Training.route) },
                    onProfile        = { nav.navigate(Dest.Profile.route) },
                    onCprGuide       = { /* TODO */ }
                )
            }

            // Reporte
            composable(REPORT_ROUTE) {
                EmergencyReportScreen(
                    onBack = { nav.popBackStack() },
                    onSubmit = { /* TODO */ }
                )
            }

            // TRAINING
            composable(Dest.Training.route) { TrainingScreen() }

            // PROTOCOLS
            composable(Dest.Protocols.route) { ProtocolsScreen(onBack = { nav.popBackStack() }) }

            // ALERTS
            composable(Dest.Alerts.route) { AlertsScreen() }

            // PROFILE — usa la MISMA instancia de auth
            composable(Dest.Profile.route) {
                val ctx = LocalContext.current
                val vm = remember(auth) {
                    ProfileViewModel(
                        auth = auth,
                        location = FusedLocationClient(ctx),
                        appContext = ctx.applicationContext,
                        devFallbackEmail = null,      // quítese el mock en prod
                        devMockLocation = null        // quítese el mock en prod
                    )
                }
                val state by vm.state.collectAsState()
                ProfileScreen(state = state, onEvent = vm::onEvent)
            }
        }
    }
}

// Marca el tab correcto aunque naveguemos a subrutas
private fun NavDestination?.isOn(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true
