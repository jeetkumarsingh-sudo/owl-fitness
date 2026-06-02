package com.example.gymdiary3.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymdiary3.domain.model.WorkoutSet
import java.util.Calendar
import androidx.compose.foundation.clickable

@Composable
fun HomeScreen(
    nav: NavHostController,
    viewModel: WorkoutViewModel = hiltViewModel(),
    bodyViewModel: BodyWeightViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentSessionId by viewModel.sessionManager.currentSessionId.collectAsStateWithLifecycle()
    val latestWeight by bodyViewModel.latestBodyWeight.collectAsStateWithLifecycle()
    val totalWorkouts by viewModel.totalWorkoutCount.collectAsStateWithLifecycle()

    val sessionDuration by viewModel.sessionDurationSeconds.collectAsStateWithLifecycle()
    val sessionNotes by viewModel.currentSessionNotes.collectAsStateWithLifecycle()
    val exercisesThisSession by viewModel.exercisesThisSession.collectAsStateWithLifecycle()
    val lastSetLogged by viewModel.lastSetLogged.collectAsStateWithLifecycle()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    var showSessionDateDialog by remember { mutableStateOf(false) }
    var selectedSessionDate by remember { mutableStateOf(0L) }

    if (showSessionDateDialog) {
        AlertDialog(
            onDismissRequest = { showSessionDateDialog = false },
            title = { Text("When did you work out?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val todayMillis = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    val yesterdayMillis = cal.timeInMillis

                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedSessionDate = todayMillis
                            showSessionDateDialog = false
                            viewModel.startSession(selectedSessionDate)
                        },
                        color = OwlColors.CardBgAlt,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Today", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedSessionDate = yesterdayMillis
                            showSessionDateDialog = false
                            viewModel.startSession(selectedSessionDate)
                        },
                        color = OwlColors.CardBgAlt,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Yesterday", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSessionDateDialog = false }) { Text("CANCEL") }
            }
        )
    }


    Box(Modifier.fillMaxSize().background(OwlColors.DeepBg)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OWL FITNESS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OwlColors.TextPrimary,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.alpha(animateFloatAsState(if (isVisible) 1f else 0f, tween(300)).value)
                )

                IconButton(onClick = { nav.navigate("settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OwlColors.TextSecondary)
                }
            }

            if (currentSessionId != null) {
                ActiveSessionPanel(
                    sessionDuration = sessionDuration,
                    sessionNotes = sessionNotes,
                    exercisesThisSession = exercisesThisSession,
                    lastSetLogged = lastSetLogged,
                    onUpdateNotes = { viewModel.updateSessionNotes(it) },
                    onContinueWorkout = { nav.navigate("muscle") },
                    onFinishSession = { viewModel.endSession { id -> nav.navigate("summary/$id") } }
                )
            } else {
                // Session Card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = OwlColors.CardBg,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                text = "READY FOR GYM?",
                                style = MaterialTheme.typography.labelMedium,
                                color = OwlColors.TextSecondary,
                                letterSpacing = 1.sp
                            )
                            
                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = { showSessionDateDialog = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OwlColors.Purple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("START NEW SESSION", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Quick Stats
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
                ) {
                    Row(
                        Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeQuickStatCard(
                            label = "WORKOUTS",
                            value = totalWorkouts.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        HomeQuickStatCard(
                            label = "BODY WEIGHT",
                            value = latestWeight?.let { "${it.weight}kg" } ?: "--",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Menu Grid (Modified to 2 items)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MenuButton(
                            text = "LOG WORKOUT",
                            icon = Icons.Default.Add,
                            color = OwlColors.Purple,
                            onClick = {
                                if (currentSessionId != null) nav.navigate("muscle")
                                else Toast.makeText(context, "Start a session first!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    item {
                        MenuButton(
                            text = "BODY WEIGHT",
                            icon = Icons.Default.MonitorWeight,
                            color = OwlColors.GreenBulk,
                            onClick = { nav.navigate("weight") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveSessionPanel(
    sessionDuration: Long,
    sessionNotes: String?,
    exercisesThisSession: List<String>,
    lastSetLogged: WorkoutSet?,
    onUpdateNotes: (String) -> Unit,
    onContinueWorkout: () -> Unit,
    onFinishSession: () -> Unit
) {
    var showNotesDialog by remember { mutableStateOf(false) }

    if (showNotesDialog) {
        var tempNotes by remember { mutableStateOf(sessionNotes ?: "") }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Session Notes") },
            text = {
                OutlinedTextField(
                    value = tempNotes,
                    onValueChange = { tempNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write something about today's session...") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateNotes(tempNotes)
                    showNotesDialog = false
                }) { Text("SAVE") }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) { Text("CANCEL") }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, OwlColors.PurpleDim)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTIVE SESSION", color = OwlColors.Purple, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                    Text(
                        text = "%02d:%02d:%02d".format(sessionDuration / 3600, (sessionDuration % 3600) / 60, sessionDuration % 60),
                        style = MaterialTheme.typography.headlineLarge,
                        color = OwlColors.TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
                IconButton(onClick = { showNotesDialog = true }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes, 
                        contentDescription = "Session Notes", 
                        tint = if (sessionNotes.isNullOrBlank()) OwlColors.PurpleDim else OwlColors.Purple,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            if (!sessionNotes.isNullOrBlank()) {
                Text(
                    text = sessionNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = OwlColors.TextMuted,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 2
                )
            }

            Spacer(Modifier.height(24.dp))

            if (exercisesThisSession.isNotEmpty()) {
                Text("EXERCISES LOGGED", color = OwlColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = exercisesThisSession.joinToString(" · "),
                    color = OwlColors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            if (lastSetLogged != null) {
                Surface(
                    color = OwlColors.CardBgAlt,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, null, tint = OwlColors.TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Last: ${lastSetLogged.exercise} — ${lastSetLogged.weight}kg × ${lastSetLogged.reps}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OwlColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onContinueWorkout,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OwlColors.Purple)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("CONTINUE WORKOUT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onFinishSession,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("FINISH SESSION", color = OwlColors.RedNegative, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HomeQuickStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = OwlColors.Purple, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Surface(
        onClick = {
            scope.launch {
                scale.animateTo(0.92f, tween(100))
                scale.animateTo(1f, tween(100))
                onClick()
            }
        },
        modifier = modifier.height(140.dp).scale(scale.value),
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                color = OwlColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
