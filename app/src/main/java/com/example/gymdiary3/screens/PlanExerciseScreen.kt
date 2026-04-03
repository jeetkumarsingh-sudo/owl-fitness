package com.example.gymdiary3.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlanExerciseScreen(nav: NavHostController, dayType: String, viewModel: WorkoutViewModel) {

    val exercises by viewModel.exercisesByDay.collectAsState()

    LaunchedEffect(dayType) {
        viewModel.selectDayType(dayType)
    }

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text(dayType, style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(20.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(exercises) { exercise ->
                Text(
                    exercise.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                nav.navigate("set/${exercise.muscle}/${Uri.encode(exercise.name)}")
                            }
                        )
                        .padding(16.dp)
                )
                HorizontalDivider()
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
