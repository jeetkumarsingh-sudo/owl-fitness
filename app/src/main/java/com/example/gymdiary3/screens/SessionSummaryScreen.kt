package com.example.gymdiary3.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.Notes
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
import com.example.gymdiary3.ui.components.PrBadge
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.system.export.ShareUtils
import com.example.gymdiary3.ui.theme.OwlColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(
    nav: NavHostController,
    sessionId: Int,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val exerciseUiStates by viewModel.exerciseUiStates.collectAsStateWithLifecycle()
    val sessionWithSets = remember(sessions, sessionId) {
        sessions.find { it.session.id == sessionId }
    }
    val context = LocalContext.current
    var summaryView by remember { mutableStateOf<android.view.View?>(null) }
    val scope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val userSettings by viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())

    Scaffold(
        modifier = Modifier.fillMaxSize().background(OwlColors.DeepBg),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SESSION SUMMARY", fontWeight = FontWeight.ExtraBold)
                        sessionWithSets?.let { s ->
                            Text(
                                SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
                                    .format(Date(s.session.startTime)),
                                color = OwlColors.TextMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwlColors.DeepBg,
                    titleContentColor = OwlColors.TextPrimary
                ),
                actions = {
                    sessionWithSets?.let { s ->
                        IconButton(onClick = {
                            val text = ShareUtils.buildShareText(s, userSettings.weightUnit)
                            ShareUtils.shareText(context, text)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Text", tint = OwlColors.Purple)
                        }
                    }
                    TextButton(
                        onClick = {
                            summaryView?.let { view ->
                                view.post {
                                    val bitmap = ShareUtils.captureView(view)
                                    ShareUtils.shareImage(context, bitmap)
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = OwlColors.Purple)
                    ) {
                        Text("SHARE IMAGE", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(OwlColors.DeepBg)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                sessionWithSets?.let { s ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(key = "stats") {
                            SummaryStatsCard(isVisible, s, userSettings.weightUnit)
                        }

                        items(s.exercises.toList(), key = { it.first }) { entry ->
                            val uiState = exerciseUiStates[entry.first] ?: ExerciseUiState(entry.first, 0.0, "Stable", false, "", 0.0, 0.0)
                            var historicBest by remember { mutableStateOf(0.0) }
                            LaunchedEffect(entry.first, s.session.id) {
                                historicBest = viewModel.getHistoricBest1RM(entry.first, s.session.id.toLong())
                            }
                            ExerciseSummaryCard(isVisible, uiState, entry.second, userSettings.weightUnit, historicBest)
                        }

                        item(key = "muscle_volume") {
                            MuscleVolumeCard(isVisible, s.volumePerMuscle, userSettings.weightUnit)
                        }

                        if (s.sets.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No exercises were logged in this session.",
                                        color = OwlColors.TextMuted,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        item(key = "done_button") {
                            val doneScale = remember { Animatable(1f) }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { 
                                    scope.launch {
                                        doneScale.animateTo(0.95f, tween(100))
                                        doneScale.animateTo(1f, tween(100))
                                    }
                                    nav.navigate("home") { popUpTo("home") { inclusive = true } } 
                                },
                                modifier = Modifier.fillMaxWidth().height(64.dp).scale(doneScale.value),
                                colors = ButtonDefaults.buttonColors(containerColor = OwlColors.Purple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("DONE", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }

            // Hidden view for capturing
            sessionWithSets?.let { s ->
                AndroidView(
                    factory = { ctx ->
                        ComposeView(ctx).apply {
                            setContent {
                                ShareableSummary(s, userSettings.weightUnit)
                            }
                        }
                    },
                    modifier = Modifier.size(0.dp),
                    update = { view ->
                        summaryView = view
                    }
                )
            }
        }
    }
}

@Composable
fun SummaryStatsCard(isVisible: Boolean, s: SessionWithSets, unit: String) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
    ) {
        val sdf = remember { SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault()) }
        val dateStr = remember(s.date) { sdf.format(Date(s.date)) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = OwlColors.CardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = OwlColors.Purple,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryStat("SETS", s.sets.size.toString())
                    SummaryStat("VOLUME", "${s.totalVolume.toInt()}$unit")
                    SummaryStat("TIME", "${s.duration / 60000}m")
                }

                if (!s.session.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = OwlColors.CardBgAlt,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Notes, null, tint = OwlColors.PurpleSoft, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(s.session.notes, style = MaterialTheme.typography.bodySmall, color = OwlColors.TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseSummaryCard(isVisible: Boolean, uiState: ExerciseUiState, sets: List<WorkoutSet>, unit: String, historicBest: Double) {
    val currentBest1rm = sets.maxOfOrNull { s ->
        if (s.weight > 0) s.weight * (1 + s.reps / 30.0) else 0.0
    } ?: 0.0
    val isNewPR = currentBest1rm > historicBest && currentBest1rm > 0.0

    val prScale = remember { Animatable(0f) }
    LaunchedEffect(isNewPR) {
        if (isNewPR) {
            prScale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            prScale.animateTo(1.0f, tween(100))
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = OwlColors.CardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                uiState.exercise.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = OwlColors.Purple,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            if (isNewPR) {
                                Spacer(Modifier.width(8.dp))
                                Box(modifier = Modifier.scale(prScale.value)) {
                                    PrBadge()
                                }
                            }
                        }
                        
                        val trendColor = when {
                            uiState.trend > 0.1 -> OwlColors.GreenPositive
                            uiState.trend < -0.1 -> OwlColors.RedNegative
                            else -> OwlColors.TextMuted
                        }
                        val trendText = when {
                            uiState.trend > 0.1 -> "+${"%.1f".format(uiState.trend)}$unit since last session"
                            uiState.trend < -0.1 -> "${"%.1f".format(uiState.trend)}$unit since last session"
                            else -> "Same weight as last session"
                        }

                        Text(
                            trendText,
                            style = MaterialTheme.typography.labelSmall,
                            color = trendColor
                        )
                    }

                    if (uiState.best1RM > 0.0) {
                        Text(
                            "Best 1RM: ${"%.0f".format(uiState.best1RM)} $unit",
                            style = MaterialTheme.typography.labelMedium,
                            color = OwlColors.TextSecondary
                        )
                    } else {
                        Text(
                            "Bodyweight",
                            style = MaterialTheme.typography.labelMedium,
                            color = OwlColors.TextMuted
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                Text(
                    uiState.recommendation,
                    style = MaterialTheme.typography.labelSmall,
                    color = OwlColors.TextMuted
                )

                Spacer(Modifier.height(12.dp))
                sets.forEach { set ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Set ${set.setNumber}", style = MaterialTheme.typography.bodyLarge, color = OwlColors.TextMuted)
                                if (set.rpe != null) {
                                    Text(
                                        " · RPE ${set.rpe}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OwlColors.PurpleSoft,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                            Text(
                                "${set.weight}$unit × ${set.reps}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = OwlColors.TextPrimary
                            )
                        }
                        if (!set.notes.isNullOrBlank()) {
                            Text(
                                set.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = OwlColors.TextMuted,
                                modifier = Modifier.padding(start = 0.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShareableSummary(sessionWithSets: SessionWithSets, unit: String) {
    Column(
        modifier = Modifier
            .width(400.dp)
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            "Gym Diary Summary",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            "Owl Fitness",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(Modifier.height(16.dp))

        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        Text("Date: ${sdf.format(Date(sessionWithSets.date))}", color = Color.Black)
        
        Spacer(Modifier.height(16.dp))

        sessionWithSets.exercises.forEach { (exercise, sets) ->
            Text(exercise, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            sets.forEach {
                Text("Set ${it.setNumber}: ${it.weight}$unit x ${it.reps}", color = Color.Black)
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.LightGray)
        Spacer(Modifier.height(8.dp))
        Text(
            "Total Volume: ${sessionWithSets.totalVolume.toInt()} $unit",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

@Composable
fun MuscleVolumeCard(isVisible: Boolean, muscleVolume: Map<String, Double>, unit: String) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = OwlColors.CardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "VOLUME BY MUSCLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = OwlColors.Purple,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                muscleVolume.filter { it.value > 0 }.forEach { (muscle, volume) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(muscle, style = MaterialTheme.typography.bodyMedium, color = OwlColors.TextSecondary)
                        Text("${volume.toInt()} $unit", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = OwlColors.TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OwlColors.Purple)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = OwlColors.TextPrimary)
    }
}
