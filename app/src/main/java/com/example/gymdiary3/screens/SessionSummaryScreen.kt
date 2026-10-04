package com.example.gymdiary3.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.presentation.state.ExerciseUiState
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.components.CountUpText
import com.example.gymdiary3.ui.components.PrBadge
import com.example.gymdiary3.ui.components.PrCelebration
import com.example.gymdiary3.ui.components.SectionLabel
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.system.export.ShareUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SessionSummaryScreen(
    nav: NavHostController,
    sessionId: Int,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val exerciseUiStates by viewModel.exerciseUiStates.collectAsStateWithLifecycle()
    val sessionWithSets = remember(sessions, sessionId) { sessions.find { it.session.id == sessionId } }
    val context = LocalContext.current
    var summaryView by remember { mutableStateOf<android.view.View?>(null) }
    val scope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val userSettings by viewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())

    // Detect a new PR anywhere in the session → fire the celebration once.
    var celebrate by remember { mutableStateOf(false) }
    LaunchedEffect(sessionWithSets) {
        val s = sessionWithSets ?: return@LaunchedEffect
        for ((exercise, sets) in s.exercises) {
            val cur = sets.maxOfOrNull { if (it.weight > 0) it.weight * (1 + it.reps / 30.0) else 0.0 } ?: 0.0
            val hist = viewModel.getHistoricBest1RM(exercise, s.session.id)
            if (cur > hist && cur > 0.0) { celebrate = true; break }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ApexScaffold(
            title = "Session Summary",
            onBack = { nav.popBackStack() },
            actions = {
                sessionWithSets?.let { s ->
                    IconButton(onClick = {
                        val text = ShareUtils.buildShareText(s, userSettings.weightUnit)
                        ShareUtils.shareText(context, text)
                    }) { Icon(Icons.Default.Share, contentDescription = "Share text", tint = Apex.AccentSoft) }
                    TextButton(
                        onClick = {
                            summaryView?.let { view -> view.post {
                                val bitmap = ShareUtils.captureView(view)
                                ShareUtils.shareImage(context, bitmap)
                            } }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Apex.AccentSoft)
                    ) { Text("IMAGE", style = MaterialTheme.typography.labelLarge) }
                }
            }
        ) { padding ->
            sessionWithSets?.let { s ->
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "stats") { SummaryStatsCard(s, userSettings.weightUnit) }

                    items(s.exercises.toList(), key = { it.first }) { entry ->
                        val uiState = exerciseUiStates[entry.first] ?: ExerciseUiState(entry.first, 0.0, "Stable", false, "", 0.0, 0.0)
                        var historicBest by remember { mutableStateOf(0.0) }
                        LaunchedEffect(entry.first, s.session.id) {
                            historicBest = viewModel.getHistoricBest1RM(entry.first, s.session.id)
                        }
                        ExerciseSummaryCard(uiState, entry.second, userSettings.weightUnit, historicBest)
                    }

                    item(key = "muscle_volume") { MuscleVolumeCard(s.volumePerMuscle, userSettings.weightUnit) }

                    if (s.sets.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                                Text("No exercises were logged in this session.", color = Apex.TextMuted, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    item(key = "done") {
                        val doneScale = remember { Animatable(1f) }
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    doneScale.animateTo(0.95f, tween(100))
                                    doneScale.animateTo(1f, tween(100))
                                }
                                nav.navigate("home") { popUpTo("home") { inclusive = true } }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp).scale(doneScale.value),
                            colors = ButtonDefaults.buttonColors(containerColor = Apex.Accent),
                            shape = RoundedCornerShape(100)
                        ) { Text("DONE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White) }
                    }
                }
            }

            // Offscreen capture surface for the shareable image
            sessionWithSets?.let { s ->
                AndroidView(
                    factory = { ctx -> ComposeView(ctx).apply { setContent { ShareableSummary(s, userSettings.weightUnit) } } },
                    modifier = Modifier.size(0.dp),
                    update = { view -> summaryView = view }
                )
            }
        }

        PrCelebration(visible = celebrate, onFinished = { celebrate = false })
    }
}

@Composable
private fun SummaryStatsCard(s: SessionWithSets, unit: String) {
    val sdf = remember { SimpleDateFormat("EEEE, MMM dd · HH:mm", Locale.getDefault()) }
    ApexPanel(accentEdge = true) {
        Column(Modifier.padding(20.dp)) {
            Text(sdf.format(Date(s.date)), style = MaterialTheme.typography.labelMedium, color = Apex.AccentSoft)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("SETS", s.sets.size)
                SummaryStat("VOLUME", s.totalVolume.toInt(), unit)
                SummaryStat("TIME", (s.duration / 60000).toInt(), "m")
            }
            if (!s.session.notes.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().background(Apex.Surface3, RoundedCornerShape(10.dp)).padding(12.dp)) {
                    Text(s.session.notes!!, style = MaterialTheme.typography.bodySmall, color = Apex.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: Int, suffix: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Apex.AccentSoft)
        Spacer(Modifier.height(2.dp))
        CountUpText(
            target = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Apex.TextPrimary,
            suffix = suffix
        )
    }
}

@Composable
private fun ExerciseSummaryCard(uiState: ExerciseUiState, sets: List<WorkoutSet>, unit: String, historicBest: Double) {
    val currentBest1rm = sets.maxOfOrNull { if (it.weight > 0) it.weight * (1 + it.reps / 30.0) else 0.0 } ?: 0.0
    val isNewPR = currentBest1rm > historicBest && currentBest1rm > 0.0

    val prScale = remember { Animatable(0f) }
    LaunchedEffect(isNewPR) {
        if (isNewPR) {
            prScale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            prScale.animateTo(1.0f, tween(100))
        }
    }

    ApexPanel(radius = Apex.radiusMd) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(uiState.exercise.uppercase(), style = MaterialTheme.typography.titleMedium, color = Apex.AccentSoft, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        if (isNewPR) {
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.scale(prScale.value)) { PrBadge() }
                        }
                    }
                    val trendColor = when {
                        uiState.trend > 0.1 -> Apex.Positive
                        uiState.trend < -0.1 -> Apex.Negative
                        else -> Apex.TextMuted
                    }
                    val trendText = when {
                        uiState.trend > 0.1 -> "+${"%.1f".format(uiState.trend)}$unit since last session"
                        uiState.trend < -0.1 -> "${"%.1f".format(uiState.trend)}$unit since last session"
                        else -> "Same weight as last session"
                    }
                    Text(trendText, style = MaterialTheme.typography.labelSmall, color = trendColor)
                }
                if (uiState.best1RM > 0.0) {
                    Text("1RM ${"%.0f".format(uiState.best1RM)} $unit", style = MaterialTheme.typography.labelMedium, color = Apex.TextSecondary)
                } else {
                    Text("Bodyweight", style = MaterialTheme.typography.labelMedium, color = Apex.TextMuted)
                }
            }

            Spacer(Modifier.height(12.dp))
            sets.forEach { set ->
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Set ${set.setNumber}", style = MaterialTheme.typography.bodyMedium, color = Apex.TextMuted)
                            if (set.rpe != null) {
                                Text(" · RPE ${set.rpe}", style = MaterialTheme.typography.bodySmall, color = Apex.AccentSoft, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        Text("${set.weight}$unit × ${set.reps}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Apex.TextPrimary)
                    }
                    if (!set.notes.isNullOrBlank()) {
                        Text(set.notes!!, style = MaterialTheme.typography.bodySmall, color = Apex.TextMuted, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MuscleVolumeCard(muscleVolume: Map<String, Double>, unit: String) {
    val filtered = muscleVolume.filter { it.value > 0 }
    if (filtered.isEmpty()) return
    ApexPanel(radius = Apex.radiusMd) {
        Column(Modifier.padding(20.dp)) {
            SectionLabel("Volume by muscle", accent = true)
            Spacer(Modifier.height(12.dp))
            filtered.forEach { (muscle, volume) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(muscle, style = MaterialTheme.typography.bodyMedium, color = Apex.TextSecondary)
                    Text("${volume.toInt()} $unit", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Apex.TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun ShareableSummary(sessionWithSets: SessionWithSets, unit: String) {
    Column(
        Modifier.width(600.dp).background(Color.White).padding(32.dp)
    ) {
        Text("WORKOUT SUMMARY", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 2.sp)
        Text("APEX FITNESS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
        Spacer(Modifier.height(24.dp))
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        Text("Date: ${sdf.format(Date(sessionWithSets.date))}", color = Color.Gray, fontSize = 14.sp)
        Text("Duration: ${sessionWithSets.duration / 60000} min", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        sessionWithSets.exercises.forEach { (exercise, sets) ->
            Text(exercise.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Black)
            sets.forEach { Text("Set ${it.setNumber}: ${it.weight}$unit x ${it.reps}", color = Color.DarkGray, fontSize = 14.sp) }
            Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.height(24.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("TOTAL VOLUME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("${sessionWithSets.totalVolume.toInt()} $unit", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Black)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("TOTAL SETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("${sessionWithSets.sets.size}", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Black)
            }
        }
    }
}
