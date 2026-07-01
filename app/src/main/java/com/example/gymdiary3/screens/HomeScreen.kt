package com.example.gymdiary3.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.settings.UserSettings
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.ui.components.ApexCard
import com.example.gymdiary3.ui.components.ApexPrimaryButton
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    nav: NavHostController,
    viewModel: WorkoutViewModel = hiltViewModel(),
    bodyViewModel: BodyWeightViewModel = hiltViewModel()
) {
    val currentSessionId by viewModel.sessionManager.currentSessionId.collectAsStateWithLifecycle()
    val latestWeight by bodyViewModel.latestBodyWeight.collectAsStateWithLifecycle()
    val totalWorkouts by viewModel.totalWorkoutCount.collectAsStateWithLifecycle()
    val userSettings by viewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(UserSettings())

    val sessionDuration by viewModel.sessionDurationSeconds.collectAsStateWithLifecycle()
    val exercisesThisSession by viewModel.exercisesThisSession.collectAsStateWithLifecycle()

    var showSessionDateDialog by remember { mutableStateOf(false) }

    if (showSessionDateDialog) {
        AlertDialog(
            onDismissRequest = { showSessionDateDialog = false },
            containerColor = OwlColors.CardBg,
            title = {
                Text(
                    text = "START EMPTY WORKOUT",
                    style = MaterialTheme.typography.titleMedium,
                    color = OwlColors.TextPrimary,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val now = System.currentTimeMillis()
                    val cal = Calendar.getInstance().apply { timeInMillis = now; add(Calendar.DAY_OF_MONTH, -1) }
                    val yesterdayStartMillis = cal.timeInMillis

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSessionDateDialog = false
                                viewModel.startSession(now)
                            },
                        color = OwlColors.Purple,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Start Now",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = OwlColors.TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSessionDateDialog = false
                                viewModel.startSession(yesterdayStartMillis)
                            },
                        color = OwlColors.CardBgAlt,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
                    ) {
                        Text(
                            text = "Log Yesterday",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = OwlColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSessionDateDialog = false }) {
                    Text("CANCEL", color = OwlColors.TextMuted, letterSpacing = 1.sp)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OwlColors.DeepBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(48.dp))
        
        Text("APEX FITNESS", style = MaterialTheme.typography.labelMedium, color = OwlColors.Purple, letterSpacing = 1.sp)
        Spacer(Modifier.height(4.dp))
        Text("Ready to work.", style = MaterialTheme.typography.headlineLarge, color = OwlColors.TextPrimary)
        
        Spacer(Modifier.height(40.dp))

        if (currentSessionId != null) {
            Surface(
                color = OwlColors.CardBg,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, OwlColors.GreenPositive.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(OwlColors.GreenPositive, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "WORKOUT IN PROGRESS",
                            style = MaterialTheme.typography.labelMedium,
                            color = OwlColors.GreenPositive,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "%02d:%02d".format(sessionDuration / 60, sessionDuration % 60),
                                style = MaterialTheme.typography.displaySmall,
                                color = OwlColors.TextPrimary,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("DURATION", style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary, letterSpacing = 1.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "${exercisesThisSession.size}",
                                style = MaterialTheme.typography.displaySmall,
                                color = OwlColors.TextPrimary,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("EXERCISES", style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary, letterSpacing = 1.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    ApexPrimaryButton(text = "RESUME WORKOUT", onClick = { nav.navigate("muscle") })
                    
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.endSession { nav.navigate("history") } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "FINISH SESSION",
                            color = OwlColors.TextSecondary,
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        } else {
            ApexPrimaryButton(
                text = "START EMPTY WORKOUT",
                onClick = { showSessionDateDialog = true }
            )
            Spacer(Modifier.height(16.dp))
            ApexCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { nav.navigate("program_tracker") }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = OwlColors.Purple)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Program Routines", style = MaterialTheme.typography.titleMedium, color = OwlColors.TextPrimary)
                        Text("Follow a structured plan", style = MaterialTheme.typography.labelMedium, color = OwlColors.TextSecondary)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OwlColors.TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(48.dp))
        Text("LIFETIME STATS", style = MaterialTheme.typography.labelMedium, color = OwlColors.TextSecondary, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ApexCard(
                modifier = Modifier.weight(1f),
                onClick = { nav.navigate("history") }
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("WORKOUTS", style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = totalWorkouts.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = OwlColors.TextPrimary
                    )
                }
            }
            
            ApexCard(
                modifier = Modifier.weight(1f),
                onClick = { nav.navigate("weight") }
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("BODY WEIGHT", style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    val weightText = latestWeight?.let { 
                        WeightFormatter.formatFromKilograms(it.weight, userSettings.weightUnit) 
                    } ?: "--"
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = weightText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = OwlColors.TextPrimary
                        )
                        if (latestWeight != null) {
                            Text(
                                text = " ${WeightFormatter.label(userSettings.weightUnit).uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = OwlColors.TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(48.dp))
    }
}
