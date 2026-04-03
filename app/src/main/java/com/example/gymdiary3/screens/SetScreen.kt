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
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var support by remember { mutableStateOf(false) }

    val lastSet by viewModel.lastSet.collectAsState()
    val suggestedWeight by viewModel.suggestedWeight.collectAsState()
    val currentSet by viewModel.currentSet.collectAsState()

    LaunchedEffect(exercise) {
        viewModel.loadLastSet(exercise)
        viewModel.updateSetNumber(exercise)
    }

    LaunchedEffect(lastSet) {
        lastSet?.let {
            weight = it.weight.toString()
            reps = it.reps.toString()
            support = it.support
        }
    }

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text(
            text = "$exercise ($muscle)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Set $currentSet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            label = { Text("Reps") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            lastSet?.let {
                Text(
                    text = "Last: ${it.weight}kg × ${it.reps}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            suggestedWeight?.let { suggestion ->
                TextButton(
                    onClick = { weight = suggestion.toString() },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = "Suggested: ${suggestion}kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row {
            Checkbox(
                checked = support,
                onCheckedChange = { support = it }
            )
            Text("Support used")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val r = reps.toIntOrNull() ?: return@Button
                val w = weight.toDoubleOrNull() ?: return@Button

                viewModel.insertWorkout(
                    muscle = muscle,
                    exercise = exercise,
                    set = currentSet,
                    reps = r,
                    weight = w,
                    support = support
                )

                // UX Improvement: Clear reps only, keep weight and increment set
                reps = ""
                viewModel.updateSetNumber(exercise)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Set")
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = { nav.popBackStack() }) {
            Text("Back")
        }
    }
}
