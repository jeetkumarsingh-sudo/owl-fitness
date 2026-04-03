package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel

@Composable
fun SetScreen(
    nav: NavHostController,
    muscle: String,
    exercise: String,
    viewModel: WorkoutViewModel
) {
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var support by remember { mutableStateOf(false) }

    Column(Modifier.padding(20.dp)) {

        Text(
            text = "$exercise ($muscle)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = sets,
            onValueChange = { sets = it },
            label = { Text("Sets") }
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            label = { Text("Reps") }
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") }
        )

        Spacer(Modifier.height(10.dp))

        Row {
            Checkbox(
                checked = support,
                onCheckedChange = { support = it }
            )
            Text("Support used")
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            val s = sets.toIntOrNull() ?: return@Button
            val r = reps.toIntOrNull() ?: return@Button
            val w = weight.toDoubleOrNull() ?: return@Button

            viewModel.insertWorkout(
                muscle = muscle,
                exercise = exercise,
                set = s,
                reps = r,
                weight = w,
                support = support
            )

            // Reset fields
            sets = ""
            reps = ""
            weight = ""
            support = false
            
            nav.popBackStack()
        }) {
            Text("Save Workout")
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = { nav.popBackStack() }) {
            Text("Back")
        }
    }
}
