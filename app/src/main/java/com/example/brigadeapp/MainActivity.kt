package com.example.brigadeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.brigadeapp.protocols.ProtocolsScreen
import com.example.brigadeapp.alerts.AlertsScreen
import com.example.brigadeapp.home.HomeScreen
import com.example.brigadeapp.nav.*
import com.example.brigadeapp.ui.theme.BrigadeAppTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrigadeAppTheme {
                val nav = rememberNavController()
                Scaffold(
                    bottomBar = {
                        val entry by nav.currentBackStackEntryAsState()
                        val current = entry?.destination?.route
                        NavigationBar {
                            bottomItems.forEach { d ->
                                NavigationBarItem(
                                    selected = current == d.route,
                                    onClick = {
                                        nav.navigate(d.route) {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        }
                                    },
                                    icon = { Icon(d.icon, d.label) },
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
                        composable(Dest.Emergency.route) { HomeScreen(
                            onEmergencyClick = { },
                            onNotifications = { nav.navigate(Dest.Alerts.route) },
                            onProtocols = { nav.navigate(Dest.Protocols.route) },
                            onTraining = { nav.navigate(Dest.Training.route) },
                            onProfile = { nav.navigate(Dest.Profile.route) },
                            onCprGuide = { }
                        ) }
                        composable(Dest.Training.route)  { Text("Training Screen") }
                        composable(Dest.Protocols.route) { ProtocolsScreen(onBack = { nav.popBackStack() }) }
                        composable(Dest.Alerts.route)    { AlertsScreen(onBack = { nav.popBackStack() }) }
                        composable(Dest.Profile.route)   { Text("Profile Screen") }
                    }

                }
            }
        }
    }
}

