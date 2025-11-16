import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.brigadeapp.R

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
fun BottomBar(nav: NavHostController, onEmergencySelected: () -> Unit) {
    val entry by nav.currentBackStackEntryAsState()
    val currentDest = entry?.destination

    NavigationBar {
        bottomItems.forEach { d ->
            NavigationBarItem(
                selected = currentDest.isOn(d.route),
                onClick = {
                    if (d.route == Dest.Emergency.route) {
                        onEmergencySelected()
                    } else {
                        nav.navigate(d.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        }
                    }
                },
                icon  = { Icon(painterResource(d.iconRes), contentDescription = d.label) },
                label = { Text(d.label) }
            )
        }
    }
}


private fun NavDestination?.isOn(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true