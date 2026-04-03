package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BodyWeightScreen(nav: NavHostController, viewModel: BodyWeightViewModel) {

    var weightInput by remember { mutableStateOf("") }
    val weights by viewModel.allWeights.collectAsState()

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text(
            "Body Weight Tracker",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = weightInput,
            onValueChange = { weightInput = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                val w = weightInput.toDoubleOrNull() ?: return@Button
                viewModel.insertWeight(w)
                weightInput = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Weight")
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(weights) { item ->
                val formattedDate = SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(Date(item.date))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formattedDate)
                        Text("${item.weight} kg")
                    }
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
