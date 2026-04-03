package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel

@Composable
fun PlanScreen(nav: NavHostController, viewModel: WorkoutViewModel) {

    val dayTypes by viewModel.dayTypes.collectAsState()

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text("Workout Plan", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(20.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(dayTypes) { day ->
                Button(
                    onClick = {
                        nav.navigate("plan_exercises/$day")
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(day)
                }
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
