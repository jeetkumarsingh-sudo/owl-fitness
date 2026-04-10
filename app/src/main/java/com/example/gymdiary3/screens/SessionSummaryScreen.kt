package com.example.gymdiary3.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
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
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.domain.WorkoutAnalyzer
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(nav: NavHostController, viewModel: WorkoutViewModel, sessionId: Int) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val sessionWithSets = remember(sessions, sessionId) {
        sessions.find { it.session.id == sessionId }
    }
    val context = LocalContext.current
    var summaryView by remember { mutableStateOf<View?>(null) }
    val scope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = { Text("SESSION SUMMARY", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    sessionWithSets?.let { s ->
                        IconButton(onClick = {
                            val text = buildShareText(s)
                            shareText(context, text)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Text", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    TextButton(onClick = {
                        summaryView?.let { view ->
                            view.post {
                                val bitmap = captureView(view)
                                shareImage(context, bitmap)
                            }
                        }
                    }) {
                        Text("SHARE IMAGE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                sessionWithSets?.let { s ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "stats") {
                            SummaryStatsCard(isVisible, s)
                        }

                        items(s.exercises.toList(), key = { it.first }) { entry ->
                            val uiState = viewModel.getExerciseUiState(entry.first)
                            ExerciseSummaryCard(isVisible, uiState, entry.second)
                        }

                        item(key = "muscle_volume") {
                            MuscleVolumeCard(isVisible, s.volumePerMuscle)
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
                                modifier = Modifier.fillMaxWidth().height(56.dp).scale(doneScale.value),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("DONE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary))
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
                                ShareableSummary(s)
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
fun SummaryStatsCard(isVisible: Boolean, s: SessionWithSets) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
    ) {
        val sdf = remember { SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault()) }
        val dateStr = remember(s.date) { sdf.format(Date(s.date)) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryStat("SETS", s.sets.size.toString())
                    SummaryStat("VOLUME", "${s.totalVolume.toInt()}kg")
                    SummaryStat("TIME", "${s.duration / 60000}m")
                }
            }
        }
    }
}

@Composable
fun ExerciseSummaryCard(isVisible: Boolean, uiState: com.example.gymdiary3.viewmodel.ExerciseUiState, sets: List<WorkoutSet>) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
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
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (uiState.isPR) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "NEW PR",
                                    color = Color(0xFFFFC107),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        Text(
                            uiState.trendLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = uiState.trendColor
                        )
                    }

                    if (uiState.best1RM > 0.0) {
                        Text(
                            "Best 1RM: ${uiState.best1RM.toInt()} kg",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Text(
                            "Bodyweight",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                Text(
                    uiState.recommendation,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(8.dp))
                sets.forEach { set ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Set ${set.setNumber}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${set.weight}kg × ${set.reps}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShareableSummary(sessionWithSets: SessionWithSets) {
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
                Text("Set ${it.setNumber}: ${it.weight}kg x ${it.reps}", color = Color.Black)
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.LightGray)
        Spacer(Modifier.height(8.dp))
        Text(
            "Total Volume: ${sessionWithSets.totalVolume.toInt()} kg",
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

fun buildShareText(sessionWithSets: SessionWithSets): String {
    val sb = StringBuilder()
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    sb.append("Owl Fitness Workout Summary\n")
    sb.append("---------------------------\n")
    sb.append("Date: ${sdf.format(Date(sessionWithSets.date))}\n")
    
    sb.append("Duration: ${sessionWithSets.duration / 60000} min\n\n")

    sessionWithSets.exercises.forEach { (exercise, sets) ->
        sb.append("$exercise\n")
        sets.forEach {
            sb.append("- Set ${it.setNumber}: ${it.weight}kg x ${it.reps}\n")
        }
        sb.append("\n")
    }

    sb.append("---------------------------\n")
    sb.append("Total Volume: ${sessionWithSets.totalVolume.toInt()} kg\n")
    sb.append("Total Sets: ${sessionWithSets.sets.size}\n")
    
    return sb.toString()
}

@Composable
fun MuscleVolumeCard(isVisible: Boolean, muscleVolume: Map<String, Double>) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "VOLUME BY MUSCLE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                muscleVolume.filter { it.value > 0 }.forEach { (muscle, volume) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(muscle, style = MaterialTheme.typography.bodyMedium)
                        Text("${volume.toInt()} kg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
