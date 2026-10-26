package org.samvid.orchit.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.samvid.orchit.ui.navigation.OrchitNavHost
import org.samvid.orchit.ui.navigation.OrchitScreen
import org.samvid.orchit.ui.navigation.OrchitWindowSize
import org.samvid.orchit.ui.theme.OrchitTheme

/**
 * Root composable for the Orchit app.
 * Entry point exposed by the UI module.
 */
@Composable
fun OrchitApp() {
    OrchitTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val windowSize = when {
                maxWidth < 600.dp -> OrchitWindowSize.Compact
                maxWidth < 840.dp -> OrchitWindowSize.Medium
                else -> OrchitWindowSize.Expanded
            }
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (windowSize == OrchitWindowSize.Compact) {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            OrchitScreen.entries.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                        )
                                    },
                                    label = { Text(screen.label) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(OrchitScreen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                )
                            }
                        }
                    },
                ) { paddingValues ->
                    OrchitNavHost(
                        navController = navController,
                        windowSize = windowSize,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail {
                        OrchitScreen.entries.forEach { screen ->
                            NavigationRailItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label,
                                    )
                                },
                                label = { Text(screen.label) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(OrchitScreen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    }
                    OrchitNavHost(
                        navController = navController,
                        windowSize = windowSize,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "OrchitApp – Compact Dark")
@Composable
private fun OrchitAppCompactDarkPreview() {
    OrchitApp()
}

@Preview(showBackground = true, widthDp = 700, name = "OrchitApp – Medium Dark")
@Composable
private fun OrchitAppMediumPreview() {
    OrchitApp()
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 700, name = "OrchitApp – Expanded")
@Composable
private fun OrchitAppExpandedPreview() {
    OrchitApp()
}
