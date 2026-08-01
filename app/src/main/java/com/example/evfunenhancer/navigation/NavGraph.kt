package com.example.evfunenhancer.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.evfunenhancer.ui.components.TicketGlyph
import com.example.evfunenhancer.ui.components.TicketNavBar
import com.example.evfunenhancer.ui.components.TicketNavItem
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

private sealed class Screen(val route: String, val glyph: TicketGlyph, val stubBrush: Brush) {
    object Profile : Screen(
        "profile", TicketGlyph.PROFILE,
        Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))
    )
    object Points : Screen(
        "points", TicketGlyph.VOTES,
        Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D)))
    )
    object Summary : Screen(
        "summary", TicketGlyph.SUMMARY,
        Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C00)))
    )
    object Aftershow : Screen(
        "aftershow", TicketGlyph.AFTERSHOW,
        Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFFFD700)))
    )
    object Maintenance : Screen(
        "maintenance", TicketGlyph.PROFILE,
        Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))
    )
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
    val isReady = username != null && selectedShow != null && roomCode != null

    val strings: AppStrings = if (language == "fi") StringsFi else StringsEn
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

    CompositionLocalProvider(LocalAppStrings provides strings) {
        Scaffold(
            bottomBar = {
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route
                val results by vm.results.collectAsState()
                val resultsUploaded = results != null

                fun navigate(route: String) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                val items = buildList {
                    SCREENS.forEach { screen ->
                        add(
                            TicketNavItem(
                                key = screen.route,
                                label = screen.label(strings),
                                glyph = screen.glyph,
                                stubBrush = screen.stubBrush,
                                selected = currentRoute == screen.route,
                                enabled = screen == Screen.Profile || isReady,
                                onClick = { navigate(screen.route) },
                            )
                        )
                    }
                    add(
                        TicketNavItem(
                            key = Screen.Aftershow.route,
                            label = "Aftershow",
                            glyph = Screen.Aftershow.glyph,
                            stubBrush = Screen.Aftershow.stubBrush,
                            selected = currentRoute == Screen.Aftershow.route,
                            enabled = isReady,
                            showHighlight = isReady && resultsUploaded,
                            onClick = { navigate(Screen.Aftershow.route) },
                        )
                    )
                }
                TicketNavBar(items = items)
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
