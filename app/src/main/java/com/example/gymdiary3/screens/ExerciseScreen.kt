package com.example.gymdiary3.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymdiary3.ui.components.EmptyState
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.WorkoutViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(nav: NavHostController, muscle: String, viewModel: WorkoutViewModel) {

    val exercises by viewModel.exercisesByMuscle.collectAsStateWithLifecycle()

    LaunchedEffect(muscle) {
        viewModel.selectMuscle(muscle)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = OwlColors.DeepBg,
        topBar = {
            TopAppBar(
                title = { Text(muscle.uppercase(), fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwlColors.DeepBg,
                    titleContentColor = OwlColors.TextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OwlColors.TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Exercise",
                            tint = OwlColors.Purple
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (exercises.isEmpty()) {
                EmptyState(
                    message = "No exercises found for this category.\nTap '+' to add your first $muscle exercise!",
                    title = muscle.uppercase() + " EXERCISES"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = exercises,
                        key = { it.name }
                    ) { exercise ->
                        var showMenu by remember { mutableStateOf(false) }

                        Box {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            nav.navigate("set/$muscle/${Uri.encode(exercise.name)}")
                                        },
                                        onLongClick = { showMenu = true }
                                    ),
                                color = OwlColors.CardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OwlColors.BorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        exercise.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OwlColors.TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = OwlColors.TextMuted
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(OwlColors.CardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete Exercise", color = OwlColors.RedNegative) },
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
            containerColor = OwlColors.CardBg,
            confirmButton = {
                Button(
                    onClick = {
                        if (newExerciseName.isNotBlank()) {
                            viewModel.addExercise(newExerciseName, muscle)
                        }
                        showAddDialog = false
                        newExerciseName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OwlColors.Purple)
                ) { Text("ADD", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    newExerciseName = "" 
                }) { Text("CANCEL", color = OwlColors.TextSecondary) }
            },
            title = { Text("Add $muscle Exercise", color = OwlColors.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it },
                    label = { Text("Exercise Name", color = OwlColors.TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = OwlColors.TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OwlColors.TextPrimary,
                        unfocusedTextColor = OwlColors.TextPrimary,
                        focusedBorderColor = OwlColors.Purple,
                        unfocusedBorderColor = OwlColors.BorderSubtle,
                        focusedLabelColor = OwlColors.Purple,
                        unfocusedLabelColor = OwlColors.TextMuted,
                        cursorColor = OwlColors.Purple,
                        focusedContainerColor = OwlColors.InputBg,
                        unfocusedContainerColor = OwlColors.InputBg
                    )
                )
            }
        )
    }
}
