package com.rescuedesk.ai.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rescuedesk.ai.R
import com.rescuedesk.ai.ui.screens.ask.AskAiScreen
import com.rescuedesk.ai.ui.screens.emergency.EmergencyHelpScreen
import com.rescuedesk.ai.ui.screens.family.MyFamilyScreen
import com.rescuedesk.ai.ui.screens.guides.GuidesScreen
import com.rescuedesk.ai.ui.screens.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val GUIDES = "guides"
    const val ASK_AI = "ask_ai"
    const val MY_FAMILY = "my_family"
    const val EMERGENCY_HELP = "emergency_help"
    const val GUIDE_DETAIL = "guide_detail"
}

private data class TabSpec(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

// PRD §4.6: exactly four tab destinations. Emergency Help is a prominent
// action, never a hidden fifth tab.
private val tabs = listOf(
    TabSpec(Routes.HOME, R.string.nav_home, Icons.Filled.Home),
    TabSpec(Routes.GUIDES, R.string.nav_guides, Icons.Filled.MenuBook),
    TabSpec(Routes.ASK_AI, R.string.nav_ask_ai, Icons.Filled.Chat),
    TabSpec(Routes.MY_FAMILY, R.string.nav_my_family, Icons.Filled.Groups)
)

@Composable
fun RescueDeskApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Routes.EMERGENCY_HELP) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            // Labels always shown: icon-only navigation is prohibited
                            // for important actions (PRD §3.1, §4.4).
                            label = {
                                Text(
                                    stringResource(tab.labelRes),
                                    fontWeight = if (currentRoute == tab.route) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onEmergencyHelp = { navController.navigate(Routes.EMERGENCY_HELP) },
                    onOpenGuides = { navController.navigate(Routes.GUIDES) },
                    onAskAi = { navController.navigate(Routes.ASK_AI) },
                    onMyFamily = { navController.navigate(Routes.MY_FAMILY) }
                )
            }
            composable(Routes.GUIDES) { GuidesScreen() }
            composable(Routes.ASK_AI) { AskAiScreen(onOpenGuides = { navController.navigate(Routes.GUIDES) }) }
            composable(Routes.MY_FAMILY) { MyFamilyScreen() }
            composable(Routes.EMERGENCY_HELP) {
                EmergencyHelpScreen(
                    onBack = { navController.popBackStack() },
                    onOpenGuides = { navController.navigate(Routes.GUIDES) }
                )
            }
        }
    }
}
