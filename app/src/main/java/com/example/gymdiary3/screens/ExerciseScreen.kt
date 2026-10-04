package com.example.gymdiary3.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.components.EmptyState
import com.example.gymdiary3.ui.components.appear
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.domain.model.*
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    nav: NavHostController,
    muscle: String,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercisesByMuscle.collectAsStateWithLifecycle()

    LaunchedEffect(muscle) { viewModel.selectMuscle(muscle) }

    var showAddDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }
    var selectedEquipment by remember { mutableStateOf(EquipmentType.OTHER) }
    var selectedPattern by remember { mutableStateOf(MovementPattern.ISOLATION) }
    var selectedTracking by remember { mutableStateOf(TrackingType.WEIGHT_REPS) }

    ApexScaffold(
        title = muscle,
        onBack = { nav.popBackStack() },
        actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add exercise", tint = Apex.AccentSoft)
            }
        }
    ) { padding ->
        if (exercises.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    message = "No exercises found for this category.\nTap '+' to add your first ${muscle.lowercase()} exercise.",
                    title = "${muscle.uppercase()} EXERCISES"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(exercises, key = { _, e -> e.name }) { index, exercise ->
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        ApexPanel(
                            modifier = Modifier.fillMaxWidth().appear(index),
                            onClick = { nav.navigate("set/$muscle/${Uri.encode(exercise.name)}") },
                            onLongClick = { showMenu = true },
                            radius = Apex.radiusMd
                        ) {
                            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    exercise.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Apex.TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Apex.TextMuted
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Apex.Surface3)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete exercise", color = Apex.Negative) },
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

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Apex.Surface2,
            confirmButton = {
                Button(
                    onClick = {
                        val sanitized = newExerciseName.trim()
                            .split(" ")
                            .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
                        if (sanitized.isNotEmpty()) {
                            viewModel.addExercise(
                                name = sanitized,
                                muscle = muscle,
                                equipment = selectedEquipment,
                                movementPattern = selectedPattern,
                                trackingType = selectedTracking
                            )
                            newExerciseName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Apex.Accent)
                ) { Text("ADD", color = androidx.compose.ui.graphics.Color.White) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newExerciseName = ""
                }) { Text("CANCEL", color = Apex.TextSecondary) }
            },
            title = { Text("Add $muscle exercise", color = Apex.TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newExerciseName,
                        onValueChange = { newExerciseName = it },
                        label = { Text("Exercise name", color = Apex.TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Apex.TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Apex.TextPrimary,
                            unfocusedTextColor = Apex.TextPrimary,
                            focusedBorderColor = Apex.Accent,
                            unfocusedBorderColor = Apex.Hairline,
                            focusedLabelColor = Apex.AccentSoft,
                            unfocusedLabelColor = Apex.TextMuted,
                            cursorColor = Apex.Accent,
                            focusedContainerColor = Apex.Surface3,
                            unfocusedContainerColor = Apex.Surface3
                        )
                    )
                    ClassificationDropdown("Equipment", EquipmentType.entries, selectedEquipment) { selectedEquipment = it }
                    ClassificationDropdown("Movement pattern", MovementPattern.entries, selectedPattern) { selectedPattern = it }
                    ClassificationDropdown("Tracking type", TrackingType.entries, selectedTracking) { selectedTracking = it }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> ClassificationDropdown(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = Apex.TextMuted) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Apex.TextPrimary,
                unfocusedTextColor = Apex.TextPrimary,
                focusedBorderColor = Apex.Accent,
                unfocusedBorderColor = Apex.Hairline,
                focusedLabelColor = Apex.AccentSoft,
                unfocusedLabelColor = Apex.TextMuted,
                focusedContainerColor = Apex.Surface3,
                unfocusedContainerColor = Apex.Surface3
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Apex.Surface3)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name.replace("_", " "), color = Apex.TextPrimary) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
