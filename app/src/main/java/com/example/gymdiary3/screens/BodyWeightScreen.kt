package com.example.gymdiary3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import com.example.gymdiary3.ui.theme.BackgroundDark
import com.example.gymdiary3.ui.theme.CardDark
import com.example.gymdiary3.ui.theme.PrimaryText
import com.example.gymdiary3.ui.theme.SecondaryText
import com.example.gymdiary3.ui.theme.Accent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyWeightScreen(nav: NavHostController, viewModel: BodyWeightViewModel) {

    var weightInput by remember { mutableStateOf("") }
    val weights by remember(viewModel) { viewModel.allWeights }.collectAsState(initial = emptyList())
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        topBar = { 
            TopAppBar(
                title = { Text("BODY WEIGHT", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = PrimaryText
                )
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Current Weight (kg)", fontSize = 18.sp, color = SecondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedLabelColor = Accent,
                            unfocusedLabelColor = SecondaryText,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark
                        )
                    )

                    Button(
                        onClick = {
                            val w = weightInput.toDoubleOrNull() ?: return@Button
                            viewModel.insertWeight(w)
                            weightInput = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text("LOG WEIGHT", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Text("HISTORY", style = MaterialTheme.typography.labelLarge, color = Accent, fontWeight = FontWeight.ExtraBold)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weights, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sdf.format(Date(item.date)), style = MaterialTheme.typography.bodyLarge, color = SecondaryText)
                            Text(
                                "${item.weight} kg",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Accent
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { nav.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark)
            ) {
                Text("BACK", fontWeight = FontWeight.Bold, color = PrimaryText)
            }
        }
    }
}
