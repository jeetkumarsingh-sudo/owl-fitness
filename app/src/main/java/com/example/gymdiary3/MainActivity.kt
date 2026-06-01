package com.example.gymdiary3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.gymdiary3.screens.*
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.ui.theme.OwlFitnessTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "HOME")
    object History : BottomNavItem("history", Icons.Default.History, "HISTORY")
    object Progress : BottomNavItem("progress", Icons.AutoMirrored.Filled.ShowChart, "PROGRESS")
    object Weight : BottomNavItem("weight", Icons.Default.MonitorWeight, "WEIGHT")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OwlFitnessTheme {
                val nav = rememberNavController()
                val bottomNavItems = listOf(BottomNavItem.Home, BottomNavItem.History, BottomNavItem.Progress, BottomNavItem.Weight)
                val rootRoutes = bottomNavItems.map { it.route }.toSet()

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by nav.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        if (currentRoute in rootRoutes) {
                            NavigationBar(
                                containerColor = OwlColors.CardBg,
                                contentColor = OwlColors.TextSecondary
                            ) {
                                bottomNavItems.forEach { item ->
                                    val selected = currentRoute == item.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { 
                                            nav.navigate(item.route) {
                                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = OwlColors.Purple,
                                            selectedTextColor = OwlColors.Purple,
                                            unselectedIconColor = OwlColors.TextSecondary,
                                            unselectedTextColor = OwlColors.TextSecondary,
                                            indicatorColor = OwlColors.PurpleDim
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = nav,
                        startDestination = "home",
                        modifier = Modifier.padding(padding)
                    ) {

                        composable("home") {
                            HomeScreen(nav)
                        }

                        composable("muscle") {
                            MuscleScreen(nav)
                        }

                        composable("exercise/{muscle}") { back ->
                            val muscle = back.arguments?.getString("muscle") ?: ""
                            ExerciseScreen(nav, muscle)
                        }

                        composable("set/{muscle}/{exercise}") { back ->
                            val muscle = back.arguments?.getString("muscle") ?: ""
                            val exercise = Uri.decode(back.arguments?.getString("exercise") ?: "")
                            SetScreen(nav, muscle, exercise)
                        }

                        composable("history") {
                            SessionHistoryScreen(nav)
                        }

                        composable("summary/{sessionId}") { back ->
                            val sessionId = back.arguments?.getString("sessionId")?.toIntOrNull() ?: 0
                            SessionSummaryScreen(nav, sessionId)
                        }

                        composable("weight") {
                            BodyWeightScreen(nav)
                        }

                        composable("progress") {
                            ProgressScreen(nav)
                        }

                        composable("analytics/{exercise}") {
                            AnalyticsScreen(nav)
                        }

                        composable("settings") {
                            val workoutViewModel: WorkoutViewModel = hiltViewModel()
                            SettingsScreen(nav) {
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
                    }
                }
            }
        }
    }
}
