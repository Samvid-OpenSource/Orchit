package org.samvid.orchit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.samvid.orchit.ui.screen.EditorScreen
import org.samvid.orchit.ui.screen.HomeScreen
import org.samvid.orchit.ui.screen.SettingsScreen

enum class OrchitScreen(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Home(route = "home", label = "Home", icon = Icons.Default.Home),
    Editor(route = "editor", label = "Editor", icon = Icons.Default.Edit),
    Settings(route = "settings", label = "Settings", icon = Icons.Default.Settings),
}

@Composable
fun OrchitNavHost(
    navController: NavHostController,
    windowSize: OrchitWindowSize,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = OrchitScreen.Home.route,
        modifier = modifier,
    ) {
        composable(OrchitScreen.Home.route) {
            HomeScreen(
                onProjectClick = {
                    navController.navigate(OrchitScreen.Editor.route) {
                        launchSingleTop = true
                    }
                },
                onNewProject = {
                    navController.navigate(OrchitScreen.Editor.route) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(OrchitScreen.Editor.route) {
            EditorScreen(windowSize = windowSize)
        }
        composable(OrchitScreen.Settings.route) {
            SettingsScreen()
        }
    }
}
