package com.example.gymdiary3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorWeight
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymdiary3.screens.*
import com.example.gymdiary3.ui.design.Gd
import com.example.gymdiary3.ui.design.GdMotion
import com.example.gymdiary3.ui.design.Hairline
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.GymDiaryTheme
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private data class Tab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val TABS = listOf(
    Tab("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
    Tab("history", "History", Icons.Outlined.History, Icons.Filled.History),
    Tab("progress", "Progress", Icons.AutoMirrored.Outlined.ShowChart, Icons.AutoMirrored.Filled.ShowChart),
    Tab("body", "Body", Icons.Outlined.MonitorWeight, Icons.Filled.MonitorWeight),
)
private val TAB_ROUTES = TABS.map { it.route }.toSet()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            GymDiaryTheme {
                val nav = rememberNavController()
                val backStack by nav.currentBackStackEntryAsState()
                val route = backStack?.destination?.route
                Scaffold(
                    containerColor = Gd.Bg,
                    bottomBar = { if (route in TAB_ROUTES) BottomBar(nav, route) }
                ) { padding ->
                    AppNavHost(
                        nav = nav,
                        modifier = Modifier.padding(padding).consumeWindowInsets(padding).imePadding(),
                        onExportCsv = { vm -> exportCsv(vm) }
                    )
                }
            }
        }
    }

    private fun exportCsv(workoutViewModel: WorkoutViewModel) {
        lifecycleScope.launch {
            val uri = workoutViewModel.exportAllDataToCsv(applicationContext)
            if (uri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Export CSV"))
            } else {
                Toast.makeText(applicationContext, "No data to export", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/** Native-feeling bar: outlined icons, filled + bright when selected, a quiet neutral indicator. */
@Composable
private fun BottomBar(nav: NavHostController, current: String?) {
    Column {
        Hairline(inset = 0.dp)
        NavigationBar(containerColor = Gd.Bg, tonalElevation = 0.dp) {
            TABS.forEach { tab ->
                val selected = current == tab.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                    label = { Text(tab.label, style = GdType.meta) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gd.Text,
                        selectedTextColor = Gd.Text,
                        unselectedIconColor = Gd.TextMuted,
                        unselectedTextColor = Gd.TextMuted,
                        indicatorColor = Gd.SurfaceRaised
                    )
                )
            }
        }
    }
}

@Composable
private fun AppNavHost(nav: NavHostController, modifier: Modifier, onExportCsv: (WorkoutViewModel) -> Unit) {
    val reduced = LocalReducedMotion.current
    fun bothTabs(a: String?, b: String?) = a in TAB_ROUTES && b in TAB_ROUTES

    NavHost(
        navController = nav,
        startDestination = "home",
        modifier = modifier,
        // Tabs cross-fade; drill-downs add a short horizontal nudge. Nothing under reduced motion.
        enterTransition = {
            when {
                reduced -> EnterTransition.None
                bothTabs(initialState.destination.route, targetState.destination.route) -> fadeIn(tween(GdMotion.Base))
                else -> fadeIn(tween(GdMotion.Base)) + slideInHorizontally(tween(GdMotion.Slow, easing = GdMotion.Ease)) { it / 12 }
            }
        },
        exitTransition = { if (reduced) ExitTransition.None else fadeOut(tween(GdMotion.Fast)) },
        popEnterTransition = { if (reduced) EnterTransition.None else fadeIn(tween(GdMotion.Base)) },
        popExitTransition = {
            when {
                reduced -> ExitTransition.None
                bothTabs(initialState.destination.route, targetState.destination.route) -> fadeOut(tween(GdMotion.Fast))
                else -> fadeOut(tween(GdMotion.Base)) + slideOutHorizontally(tween(GdMotion.Slow, easing = GdMotion.Ease)) { it / 12 }
            }
        }
    ) {
        // Tabs
        composable("home") { HomeRoute(nav) }
        composable("history") { SessionHistoryScreen(nav) }
        composable("progress") { ProgressScreen(nav) }
        composable("body") { BodyWeightScreen(nav) }

        // Workout flow
        composable("workout") { ActiveWorkoutRoute(nav) }
        composable("picker") { ExercisePickerRoute(nav) }
        composable("set/{muscle}/{exercise}") { LoggerRoute(nav) }

        // Detail
        composable("summary/{sessionId}") { back ->
            SessionSummaryScreen(nav, sessionId = back.arguments?.getString("sessionId")?.toIntOrNull() ?: 0)
        }
        composable("analytics/{exercise}") { AnalyticsScreen(nav) }
        composable("program_tracker") { ProgramTrackerScreen(nav) }
        composable("program_log/{sessionId}") { back ->
            ProgramSessionLogScreen(nav, sessionId = back.arguments?.getString("sessionId")?.toIntOrNull() ?: 0)
        }
        composable("settings") {
            val workoutViewModel: WorkoutViewModel = hiltViewModel()
            SettingsScreen(nav = nav, onExportClick = { onExportCsv(workoutViewModel) })
        }
    }
}
