package com.example.brigadeapp.view.components

import BottomBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.viewmodel.utils.ConnectivityViewModel

private const val REPORT_ROUTE = "report"
private const val RCP_ROUTE = "RCP"

private const val CPR_COURSE_ROUTE = "training/cpr"
private const val TRAINING_CPR_QUIZ_ROUTE = "training_cpr_quiz"

@Composable
fun AppScaffold(auth: AuthClient) {
    val nav = rememberNavController()
    val lastEmergencyRoute = remember { mutableStateOf(Dest.Emergency.route) }

    Scaffold(
        bottomBar = { BottomBar(nav) {
            val target = lastEmergencyRoute.value
            // Try to pop back to the target; popBackStack returns true if it was on the back stack
            val popped = nav.popBackStack(target, false)
            if (!popped) {
                // Not on back stack — navigate normally while preserving state
                nav.navigate(target) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                }
            }
        } }
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
                        lastEmergencyRoute.value = REPORT_ROUTE
                        nav.navigate(REPORT_ROUTE)
                    },
                    onNotifications = { nav.navigate(Dest.Alerts.route) },
                    onProtocols    = { nav.navigate(Dest.Protocols.route) },
                    onTraining     = { nav.navigate(Dest.Training.route) },
                    onProfile      = { nav.navigate(Dest.Profile.route) },
                    onCprGuide     = {
                        lastEmergencyRoute.value = RCP_ROUTE
                        nav.navigate(RCP_ROUTE)
                    }
                )
            }

            composable(REPORT_ROUTE) {
                EmergencyReportScreen(auth = auth, onBack = { nav.popBackStack() })
            }

            composable(RCP_ROUTE) {
                RcpScreen(auth = auth, onBack = { nav.popBackStack() })
            }

            composable(Dest.Training.route)  {
                TrainingScreen(
                    onOpenCpr = { nav.navigate(CPR_COURSE_ROUTE) }
                )
            }
            composable(CPR_COURSE_ROUTE) {
                CprCourseScreen(onBack = { nav.popBackStack() })
            }



            composable(Dest.Protocols.route) { ProtocolsScreen(onBack = { nav.popBackStack() }) }
            composable(Dest.Alerts.route)    { AlertsScreen() }

            composable(Dest.Profile.route) {
                val connectivityVM: ConnectivityViewModel = hiltViewModel()
                val isOnline by connectivityVM.isOnline.collectAsState()

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
                ProfileScreen(state = state, onEvent = vm::onEvent, isOnline = isOnline)
            }
        }
    }
}
