package com.example.brigadeapp.view.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brigadeapp.data.sensors.LocationSensorImpl
import com.example.brigadeapp.domain.entity.AuthClient
import com.example.brigadeapp.view.screens.*
import com.example.brigadeapp.viewmodel.screens.ProfileViewModel

private const val REPORT_ROUTE = "report"

@Composable
fun AppScaffold(auth: AuthClient) {
    val nav = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(nav) } // <--- usar el BottomBar del archivo BottomNav.kt
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Dest.Emergency.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(Dest.Emergency.route) {
                HomeScreen(
                    auth = auth,
                    onEmergencyClick = {
                        nav.navigate(REPORT_ROUTE) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNotifications = { nav.navigate(Dest.Alerts.route) },
                    onProtocols    = { nav.navigate(Dest.Protocols.route) },
                    onTraining     = { nav.navigate(Dest.Training.route) },
                    onProfile      = { nav.navigate(Dest.Profile.route) },
                    onCprGuide     = { /* TODO */ }
                )
            }

            composable(REPORT_ROUTE) {
                EmergencyReportScreen(auth = auth, onBack = { nav.popBackStack() })
            }

            composable(Dest.Training.route)  { TrainingScreen() }
            composable(Dest.Protocols.route) { ProtocolsScreen(onBack = { nav.popBackStack() }) }
            composable(Dest.Alerts.route)    { AlertsScreen() }

            composable(Dest.Profile.route) {
                val ctx = LocalContext.current
                val vm = remember(auth) {
                    ProfileViewModel(
                        auth = auth,
                        location = LocationSensorImpl(ctx),
                        appContext = ctx.applicationContext,
                        devFallbackEmail = null,
                        devMockLocation = null
                    )
                }
                val state by vm.state.collectAsState()
                ProfileScreen(state = state, onEvent = vm::onEvent)
            }
        }
    }
}
