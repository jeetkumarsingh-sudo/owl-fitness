package com.example.gymdiary3.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun MuscleScreen(nav: NavHostController) {

    val muscles = listOf(
        "Back",
        "Chest",
        "Legs",
        "Shoulders",
        "Biceps",
        "Triceps",
        "Abs"
    )

    Column(
        modifier = Modifier.padding(20.dp)
    ) {

        Text(
            text = "Select Muscle",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        muscles.forEach { muscle ->

            Button(
                onClick = {
                    nav.navigate("exercise/$muscle")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(muscle)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}