package com.example.brigadeapp.view.components

import com.example.brigadeapp.view.components.BottomBar
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.brigadeapp.data.source.local.sensors.LocationSensorImpl
import com.example.brigadeapp.domain.entity.AuthClient
import com.example.brigadeapp.view.screens.*
import com.example.brigadeapp.viewmodel.screens.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.viewmodel.utils.ConnectivityViewModel

private const val REPORT_ROUTE = "report"
private const val REPORTS_LIST_ROUTE = "reports_list"
private const val RCP_ROUTE = "RCP"
private const val TRAINING_ROUTE = "training/{trainingId}/{trainingTitle}"
private const val LEADERBOARD_ROUTE = "training/leaderboard"
private const val RAG_ROUTE = "rag"

@Composable
fun AppScaffold(auth: AuthClient) {
    val nav = rememberNavController()
    val lastEmergencyRoute = remember { mutableStateOf(Dest.Emergency.route) }
    val entry by nav.currentBackStackEntryAsState()

    LaunchedEffect(entry?.destination?.route) {
        if (entry?.destination?.route == Dest.Emergency.route) {
            lastEmergencyRoute.value = Dest.Emergency.route
        }
    }

    Scaffold(
        bottomBar = { BottomBar(nav) {
            val target = lastEmergencyRoute.value

            val popped = nav.popBackStack(target, false)
            if (!popped) {
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
                    onViewReportsList = {
                        lastEmergencyRoute.value = REPORTS_LIST_ROUTE
                        nav.navigate(REPORTS_LIST_ROUTE)
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

            composable(REPORTS_LIST_ROUTE) {
                ReportsListScreen(
                    onBack = { nav.popBackStack() },
                    onReportClick = { /* TODO: navigate to report detail */ }
                )
            }

            composable(RCP_ROUTE) {
                RcpScreen(auth = auth, onBack = { nav.popBackStack() })
            }

            composable(Dest.Training.route)  {
                TrainingScreen(
                    onOpenTraining = { trainingId, title ->
                        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                        nav.navigate("training/$trainingId/$encodedTitle")
                    },
                    onOpenLeaderboard = {
                        nav.navigate(LEADERBOARD_ROUTE)
                    },
                    onBack = { nav.popBackStack() }
                )
            }

            composable(TRAINING_ROUTE) { backStackEntry ->
                val trainingId = backStackEntry.arguments?.getString("trainingId") ?: ""
                val encodedTitle = backStackEntry.arguments?.getString("trainingTitle") ?: ""
                val trainingTitle = java.net.URLDecoder.decode(encodedTitle, "UTF-8")
                
                GenericTrainingScreen(
                    trainingId = trainingId,
                    trainingTitle = trainingTitle,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(LEADERBOARD_ROUTE) {
                TrainingLeaderboardScreen(
                    onBack = { nav.popBackStack() }
                )
            }

            composable(Dest.Protocols.route) {
                ProtocolsScreen(
                    onBack = { nav.popBackStack() },
                    onNavigateToRag = {
                        nav.navigate(RAG_ROUTE)
                    }
                )
            }

            composable(RAG_ROUTE) {
                RagScreen(
                    onBack = { nav.popBackStack() }
                )
            }

            composable(Dest.Alerts.route) {
                AlertsScreen(onBack = { nav.popBackStack() })
            }

            composable(Dest.Profile.route) {
                val connectivityVM: ConnectivityViewModel = hiltViewModel()
                val isOnline by connectivityVM.isOnline.collectAsState()

                val ctx = LocalContext.current
                val locationSensor = remember { LocationSensorImpl(ctx) }
                val vm = remember(auth) {
                    ProfileViewModel(
                        auth = auth,
                        location = locationSensor,
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