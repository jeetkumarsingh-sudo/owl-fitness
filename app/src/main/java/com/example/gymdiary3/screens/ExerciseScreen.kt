package com.example.gymdiary3.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.ui.theme.BackgroundDark
import com.example.gymdiary3.ui.theme.CardDark
import com.example.gymdiary3.ui.theme.PrimaryText
import com.example.gymdiary3.ui.theme.SecondaryText
import com.example.gymdiary3.ui.theme.Accent

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(nav: NavHostController, muscle: String, viewModel: WorkoutViewModel) {

    val exercises by remember(viewModel) { viewModel.exercisesByMuscle }.collectAsState()

    LaunchedEffect(muscle) {
        viewModel.selectMuscle(muscle)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        topBar = {
            TopAppBar(
                title = { Text(muscle.uppercase(), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = PrimaryText,
                    actionIconContentColor = Accent
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium, color = Accent)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(BackgroundDark)) {
            if (exercises.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No exercises yet", color = SecondaryText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = exercises,
                        key = { it.name }
                    ) { exercise ->
                        var showMenu by remember { mutableStateOf(false) }

                        Box {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            nav.navigate("set/$muscle/${Uri.encode(exercise.name)}")
                                        },
                                        onLongClick = { showMenu = true }
                                    ),
                                colors = CardDefaults.cardColors(containerColor = CardDark),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        exercise.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(CardDark)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete Exercise", color = Color.Red) },
                                    onClick = {
                                        viewModel.deleteExercise(exercise)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CardDark,
            confirmButton = {
                Button(
                    onClick = {
                        if (newExerciseName.isNotBlank()) {
                            viewModel.addExercise(newExerciseName, muscle)
                        }
                        showAddDialog = false
                        newExerciseName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) { Text("ADD", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    newExerciseName = "" 
                }) { Text("CANCEL", color = SecondaryText) }
            },
            title = { Text("Add $muscle Exercise", color = PrimaryText) },
            text = {
                OutlinedTextField(
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it },
                    label = { Text("Exercise Name", color = SecondaryText) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = PrimaryText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = SecondaryText
                    )
                )
            }
        )
    }
}
