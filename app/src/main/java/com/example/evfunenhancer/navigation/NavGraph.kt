package com.example.evfunenhancer.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.evfunenhancer.ui.screens.AfterShowScreen
import com.example.evfunenhancer.ui.screens.MaintenanceScreen
import com.example.evfunenhancer.ui.screens.PointsScreen
import com.example.evfunenhancer.ui.screens.SummaryScreen
import com.example.evfunenhancer.ui.screens.UsernameShowScreen
import com.example.evfunenhancer.ui.strings.AppStrings
import com.example.evfunenhancer.ui.strings.LocalAppStrings
import com.example.evfunenhancer.ui.strings.StringsEn
import com.example.evfunenhancer.ui.strings.StringsFi
import com.example.evfunenhancer.viewmodel.MainViewModel

private sealed class Screen(val route: String, val icon: ImageVector) {
    object Profile : Screen("profile", Icons.Default.Person)
    object Points : Screen("points", Icons.Default.TableChart)
    object Summary : Screen("summary", Icons.Default.Leaderboard)
    object Aftershow : Screen("aftershow", Icons.Default.AutoAwesome)
    object Maintenance : Screen("maintenance", Icons.Default.Build)
}

private fun Screen.label(s: AppStrings): String = when (this) {
    Screen.Profile -> s.profileTab
    Screen.Points -> s.pointsTab
    Screen.Summary -> s.summaryTab
    Screen.Aftershow -> "Aftershow"
    Screen.Maintenance -> s.maintenanceMode
}

private val SCREENS = listOf(Screen.Profile, Screen.Points, Screen.Summary)

@Composable
fun NavGraph(vm: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val username by vm.username.collectAsState()
    val roomCode by vm.roomCode.collectAsState()
    val selectedShow by vm.selectedShowId.collectAsState()
    val language by vm.language.collectAsState()
    val results by vm.results.collectAsState()
    val isReady = username != null && selectedShow != null && roomCode != null
    val isFinalSelected = selectedShow == "final" && roomCode != null

    val strings: AppStrings = if (language == "fi") StringsFi else StringsEn
    var showNoResultsDialog by remember { mutableStateOf(false) }
    val disclaimerAccepted by vm.disclaimerAccepted.collectAsState()
    var showDisclaimerDialog by remember { mutableStateOf(false) }

    if (!disclaimerAccepted || showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { if (disclaimerAccepted) showDisclaimerDialog = false },
            title = { Text(strings.disclaimerTitle, style = MaterialTheme.typography.titleLarge) },
            text = { Text(strings.disclaimerBody) },
            confirmButton = {
                TextButton(onClick = {
                    vm.acceptDisclaimer()
                    showDisclaimerDialog = false
                }) { Text(strings.disclaimerButton) }
            }
        )
    }

    if (showNoResultsDialog) {
        AlertDialog(
            onDismissRequest = { showNoResultsDialog = false },
            title = { Text(strings.aftershowNotAvailableTitle, style = MaterialTheme.typography.titleLarge) },
            text = { Text(strings.aftershowNotAvailableBody) },
            confirmButton = {
                TextButton(onClick = { showNoResultsDialog = false }) { Text("OK") }
            }
        )
    }

    CompositionLocalProvider(LocalAppStrings provides strings) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val backStack by navController.currentBackStackEntryAsState()
                    val currentRoute = backStack?.destination?.route
                    val navItemColors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.50f),
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.50f),
                        disabledIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                        disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                    )
                    SCREENS.forEach { screen ->
                        val enabled = screen == Screen.Profile || isReady
                        val label = screen.label(strings)
                        NavigationBarItem(
                            colors = navItemColors,
                            icon = { Icon(screen.icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentRoute == screen.route,
                            enabled = enabled,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    val resultsReady = results != null
                    val aftershowColors = if (!isFinalSelected || !resultsReady) {
                        NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                            selectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                        )
                    } else navItemColors
                    NavigationBarItem(
                        colors = aftershowColors,
                        icon = {
                            if (isFinalSelected && !resultsReady) {
                                BadgedBox(badge = { Badge(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) { Text("?") } }) {
                                    Icon(Screen.Aftershow.icon, contentDescription = "Aftershow")
                                }
                            } else {
                                Icon(Screen.Aftershow.icon, contentDescription = "Aftershow")
                            }
                        },
                        label = { Text("Aftershow") },
                        selected = currentRoute == Screen.Aftershow.route,
                        enabled = isFinalSelected,
                        onClick = {
                            if (resultsReady) {
                                navController.navigate(Screen.Aftershow.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                showNoResultsDialog = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Profile.route,
                modifier = Modifier.padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
            ) {
                composable(Screen.Profile.route) {
                    UsernameShowScreen(
                        vm,
                        onNavigateToMaintenance = { navController.navigate(Screen.Maintenance.route) },
                        currentLanguage = language,
                        onLanguageChange = vm::setLanguage,
                        onShowDisclaimer = { showDisclaimerDialog = true }
                    )
                }
                composable(Screen.Points.route) { PointsScreen(vm) }
                composable(Screen.Summary.route) { SummaryScreen(vm) }
                composable(Screen.Aftershow.route) { AfterShowScreen(vm) }
                composable(Screen.Maintenance.route) {
                    MaintenanceScreen(vm, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
