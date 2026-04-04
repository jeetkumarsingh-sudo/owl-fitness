package com.example.gymdiary3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.ui.theme.BackgroundDark
import com.example.gymdiary3.ui.theme.CardDark
import com.example.gymdiary3.ui.theme.PrimaryText
import com.example.gymdiary3.ui.theme.SecondaryText
import com.example.gymdiary3.ui.theme.Accent

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

    val repsFocusRequester = remember { FocusRequester() }
    val weightFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    val lastSet by viewModel.lastSet.collectAsState()
    val suggestedWeight by viewModel.suggestedWeight.collectAsState()
    val currentSet by viewModel.currentSet.collectAsState()
    val timer by viewModel.timer.collectAsState()

    val canLogSet = remember(reps, weight) {
        reps.isNotEmpty() && weight.isNotEmpty() && 
        reps.toIntOrNull() != null && weight.toDoubleOrNull() != null
    }

    LaunchedEffect(exercise) {
        viewModel.loadLastSet(exercise)
        viewModel.updateSetNumber(exercise)
    }

    LaunchedEffect(Unit) {
        repsFocusRequester.requestFocus()
    }

    LaunchedEffect(lastSet) {
        lastSet?.let {
            if (weight.isEmpty()) {
                weight = it.weight.toString()
            }
            support = it.support
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
            .imePadding()
            .padding(16.dp)
    ) {

        Text(
            text = exercise,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryText,
            maxLines = 1
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set $currentSet",
                style = MaterialTheme.typography.headlineSmall,
                color = Accent,
                fontWeight = FontWeight.Bold
            )

            if (timer > 0) {
                Text(
                    text = "${timer}s",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    ),
                    color = if (timer < 10) Color.Red else PrimaryText
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        TextField(
            value = weight,
            onValueChange = { weight = it },
            textStyle = TextStyle(
                color = PrimaryText,
                fontSize = 18.sp
            ),
            label = {
                Text(
                    "Weight (kg)",
                    color = Accent
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
                focusedIndicatorColor = Accent,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = Accent
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(weightFocusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = reps,
            onValueChange = { reps = it },
            textStyle = TextStyle(
                color = PrimaryText,
                fontSize = 18.sp
            ),
            label = {
                Text(
                    "Reps",
                    color = Accent
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
                focusedIndicatorColor = Accent,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = Accent
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(repsFocusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            lastSet?.let {
                Text(
                    text = "Last: ${it.weight}kg × ${it.reps}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText
                )
            }

            suggestedWeight?.let { suggestion ->
                TextButton(
                    onClick = { weight = suggestion.toString() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Suggest: ${suggestion}kg",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = support,
                onCheckedChange = { support = it },
                modifier = Modifier.size(48.dp),
                colors = CheckboxDefaults.colors(checkedColor = Accent)
            )
            Text(
                "Support / Assisted", 
                style = MaterialTheme.typography.bodyLarge,
                color = PrimaryText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val r = reps.toIntOrNull() ?: return@Button
                    val w = weight.toDoubleOrNull() ?: return@Button

                    viewModel.insertWorkout(
                        muscle = muscle,
                        exercise = exercise,
                        setNumber = currentSet,
                        reps = r,
                        weight = w,
                        support = support
                    )

                    reps = ""
                    repsFocusRequester.requestFocus()
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                enabled = canLogSet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    disabledContainerColor = Color.Gray
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "LOG SET",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { nav.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryText),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
            ) {
                Text(
                    "FINISH EXERCISE",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
