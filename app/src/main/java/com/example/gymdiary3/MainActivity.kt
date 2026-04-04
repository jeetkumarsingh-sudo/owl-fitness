package com.example.gymdiary3

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.*
import com.example.gymdiary3.database.WorkoutDatabase
import com.example.gymdiary3.screens.*
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.ui.theme.OwlFitnessTheme

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

        setContent {
            OwlFitnessTheme {
                val nav = rememberNavController()

                NavHost(
                    navController = nav,
                    startDestination = "home",
                    enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(300)) { it } },
                    exitTransition = { fadeOut(tween(250)) + slideOutHorizontally(tween(300)) { -it } },
                    popEnterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(300)) { -it } },
                    popExitTransition = { fadeOut(tween(250)) + slideOutHorizontally(tween(300)) { it } }
                ) {

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
