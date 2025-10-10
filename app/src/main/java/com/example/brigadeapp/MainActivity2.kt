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
import com.example.brigadeapp.presentation.ui.ProtocolsScreen
import com.example.brigadeapp.presentation.ui.AlertsScreen
import com.example.brigadeapp.presentation.ui.HomeScreen
import com.example.brigadeapp.presentation.ui.EmergencyReportScreen
import com.example.brigadeapp.ui.theme.BrigadeAppTheme
import androidx.compose.ui.res.painterResource
import com.example.brigadeapp.presentation.ui.Dest
import com.example.brigadeapp.presentation.ui.bottomItems

class MainActivity2 : ComponentActivity() {
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
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = d.iconRes),   // 👈 usa tu drawable
                                            contentDescription = d.label
                                        )
                                    },
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
                            onEmergencyClick = { nav.navigate("report") },
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
                            onCprGuide = { }
                        ) }
                        composable("report") {
                            EmergencyReportScreen(
                                onBack = { nav.popBackStack() },
                                onSubmit = { }
                            )
                        }
                        composable(Dest.Training.route)  { Text("Training Screen") }
                        composable(Dest.Protocols.route) { ProtocolsScreen(onBack = { nav.popBackStack() }) }
                        composable(Dest.Alerts.route)    { AlertsScreen() }
                        composable(Dest.Profile.route)   { Text("Profile Screen") }
                    }


                }
            }
        }
    }
}

