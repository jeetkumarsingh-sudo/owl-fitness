package com.example.gymdiary3

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.navigation.compose.*
import com.example.gymdiary3.database.WorkoutDatabase
import com.example.gymdiary3.screens.*
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import androidx.compose.ui.graphics.Color
import com.example.gymdiary3.viewmodel.WorkoutViewModel

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
                    return WorkoutViewModel(workoutDao) as T
                }
            }
        }

        val bodyWeightViewModel by viewModels<BodyWeightViewModel> {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BodyWeightViewModel(bodyDao) as T
                }
            }
        }

        val customDarkColorScheme = darkColorScheme(
            background = Color(0xFF121212),
            surface = Color(0xFF121212),
            surfaceVariant = Color(0xFF1E1E1E),
            onBackground = Color.White,
            onSurface = Color.White,
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            error = Color(0xFFCF6679)
        )

        val customLightColorScheme = lightColorScheme(
            background = Color.White,
            surface = Color.White,
            surfaceVariant = Color(0xFFF5F5F5)
        )

        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            MaterialTheme(colorScheme = if (isDarkTheme) customDarkColorScheme else customLightColorScheme) {
                val nav = rememberNavController()

                NavHost(navController = nav, startDestination = "home") {

                    composable("home") {
                        HomeScreen(nav, workoutViewModel, applicationContext)
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
                }
            }
        }
    }
}
