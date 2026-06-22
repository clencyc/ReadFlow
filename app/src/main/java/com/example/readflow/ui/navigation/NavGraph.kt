package com.example.readflow.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.readflow.ui.screens.PlayerScreen
import com.example.readflow.ui.screens.QueueScreen
import com.example.readflow.ui.screens.SearchScreen
import com.example.readflow.ui.screens.SettingsScreen
import com.example.readflow.ui.theme.Background
import com.example.readflow.ui.theme.CardBackground
import com.example.readflow.ui.theme.Primary
import com.example.readflow.ui.theme.TextSecondary
import com.example.readflow.ui.viewmodel.ArticleViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Queue    : Screen("queue",    "Queue",    Icons.Rounded.Headphones)
    object Search   : Screen("search",  "Search",   Icons.Rounded.Search)
    object History  : Screen("history", "History",  Icons.Rounded.History)
    object Settings : Screen("settings","Settings", Icons.Rounded.Settings)
    object Player   : Screen("player",  "Player",   Icons.Rounded.Headphones)
}

private val bottomNavItems = listOf(Screen.Queue, Screen.Search, Screen.Settings)

@Composable
fun ReadFlowApp() {
    val navController = rememberNavController()
    val viewModel: ArticleViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route != Screen.Player.route

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = CardBackground,
                    contentColor = Primary
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(screen.icon, contentDescription = screen.label)
                            },
                            label = { Text(screen.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Queue.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Queue.route) {
                QueueScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Player.route) {
                PlayerScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Search.route) {
                SearchScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
        }
    }
}