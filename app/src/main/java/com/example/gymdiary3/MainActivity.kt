package com.example.gymdiary3

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.*
import com.example.gymdiary3.database.WorkoutDatabase
import com.example.gymdiary3.screens.*
import com.example.gymdiary3.domain.settings.UserSettingsRepository
import com.example.gymdiary3.viewmodel.SettingsViewModel
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import com.example.gymdiary3.ui.theme.OwlFitnessTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = WorkoutDatabase.getDatabase(this)
        val workoutDao = database.workoutDao()
        val bodyDao = database.bodyWeightDao()

        val workoutViewModel by viewModels<WorkoutViewModel> {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WorkoutViewModel(workoutDao, UserSettingsRepository(applicationContext)) as T
                }
            }
        }

        val bodyWeightViewModel by viewModels<BodyWeightViewModel> {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BodyWeightViewModel(bodyDao, UserSettingsRepository(applicationContext)) as T
                }
            }
        }

        val settingsViewModel by viewModels<SettingsViewModel> {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(UserSettingsRepository(applicationContext)) as T
                }
            }
        }

        setContent {
            Log.d("PERF", "Root Recompose")
            OwlFitnessTheme {
                val nav = rememberNavController()

                NavHost(
                    navController = nav,
                    startDestination = "home"
                ) {

                    composable("home") {
                        HomeScreen(nav, workoutViewModel, bodyWeightViewModel, applicationContext)
                    }

                    composable("muscle") {
                        MuscleScreen(nav)
                    }

                    composable("exercise/{muscle}") { back ->
                        val muscle = back.arguments?.getString("muscle") ?: ""
                        ExerciseScreen(nav, muscle, workoutViewModel)
                    }

                    composable("set/{muscle}/{exercise}") { back ->
                        val muscle = back.arguments?.getString("muscle") ?: ""
                        val exercise = Uri.decode(back.arguments?.getString("exercise") ?: "")
                        SetScreen(nav, muscle, exercise, workoutViewModel)
                    }

                    composable("history") {
                        SessionHistoryScreen(nav, workoutViewModel)
                    }

                    composable("summary/{sessionId}") { back ->
                        val sessionId = back.arguments?.getString("sessionId")?.toIntOrNull() ?: 0
                        SessionSummaryScreen(nav, workoutViewModel, sessionId)
                    }

                    composable("weight") {
                        BodyWeightScreen(nav, bodyWeightViewModel)
                    }

                    composable("progress") {
                        ProgressScreen(nav, workoutViewModel)
                    }

                    composable("graph") {
                        ProgressGraphScreen(nav, workoutViewModel)
                    }

                    composable("settings") {
                        SettingsScreen(nav, settingsViewModel)
                    }
                }
            }
        }
    }
}
