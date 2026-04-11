package com.example.gymdiary3.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.presentation.state.ExerciseUiState
import com.example.gymdiary3.ui.components.PrBadge
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.gymdiary3.ui.theme.OwlColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(nav: NavHostController, viewModel: WorkoutViewModel, sessionId: Int) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val exerciseUiStates by viewModel.exerciseUiStates.collectAsStateWithLifecycle()
    val sessionWithSets = remember(sessions, sessionId) {
        sessions.find { it.session.id == sessionId }
    }
    val context = LocalContext.current
    var summaryView by remember { mutableStateOf<View?>(null) }
    val scope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val userSettings by if (viewModel.settingsRepository != null) {
        viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    } else {
        remember { mutableStateOf(com.example.gymdiary3.domain.settings.UserSettings()) }
    }

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
                            val text = buildShareText(s, userSettings.weightUnit)
                            shareText(context, text)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Text", tint = OwlColors.Purple)
                        }
                    }
                    TextButton(
                        onClick = {
                            summaryView?.let { view ->
                                view.post {
                                    val bitmap = captureView(view)
                                    shareImage(context, bitmap)
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
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = MaterialTheme.shapes.medium
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
                    modifier = Modifier.size(0.dp), // Keep it hidden but part of the hierarchy
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
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryStat("SETS", s.sets.size.toString())
                    SummaryStat("VOLUME", "${s.totalVolume.toInt()}$unit")
                    SummaryStat("TIME", "${s.duration / 60000}m")
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
                                PrBadge()
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Set ${set.setNumber}", style = MaterialTheme.typography.bodyLarge, color = OwlColors.TextMuted)
                        Text(
                            "${set.weight}$unit × ${set.reps}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = OwlColors.TextPrimary
                        )
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
            .width(400.dp) // Fixed width for consistent image size
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

fun captureView(view: View): Bitmap {
    // Measure and layout the view if it hasn't been done (since it's size 0 in UI)
    val widthSpec = View.MeasureSpec.makeMeasureSpec(view.resources.displayMetrics.widthPixels, View.MeasureSpec.AT_MOST)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    view.measure(widthSpec, heightSpec)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)

    val bitmap = Bitmap.createBitmap(
        view.measuredWidth,
        view.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

fun shareImage(context: Context, bitmap: Bitmap) {
    val file = File(context.cacheDir, "workout_summary.png")
    FileOutputStream(file).use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share Workout Image"))
}

fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Workout Text"))
}

fun buildShareText(sessionWithSets: SessionWithSets, unit: String): String {
    val sb = StringBuilder()
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    sb.append("Owl Fitness Workout Summary\n")
    sb.append("---------------------------\n")
    sb.append("Date: ${sdf.format(Date(sessionWithSets.date))}\n")
    
    sb.append("Duration: ${sessionWithSets.duration / 60000} min\n\n")

    sessionWithSets.exercises.forEach { (exercise, sets) ->
        sb.append("$exercise\n")
        sets.forEach {
            sb.append("- Set ${it.setNumber}: ${it.weight}$unit x ${it.reps}\n")
        }
        sb.append("\n")
    }

    sb.append("---------------------------\n")
    sb.append("Total Volume: ${sessionWithSets.totalVolume.toInt()} $unit\n")
    sb.append("Total Sets: ${sessionWithSets.sets.size}\n")
    
    return sb.toString()
}

@Composable
fun MuscleVolumeCard(isVisible: Boolean, muscleVolume: Map<String, Double>, unit: String) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "VOLUME BY MUSCLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                muscleVolume.filter { it.value > 0 }.forEach { (muscle, volume) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(muscle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${volume.toInt()} $unit", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
