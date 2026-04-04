package com.example.gymdiary3.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.SessionSummary
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(nav: NavHostController, viewModel: WorkoutViewModel, sessionId: Int) {
    val summary by viewModel.summary.collectAsState()
    val context = LocalContext.current
    var summaryView by remember { mutableStateOf<View?>(null) }

    LaunchedEffect(sessionId) {
        viewModel.loadSummary(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Summary", fontWeight = FontWeight.Bold) },
                actions = {
                    summary?.let { s ->
                        IconButton(onClick = {
                            val text = buildShareText(s)
                            shareText(context, text)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Text")
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
                        Text("SHARE IMAGE")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                summary?.let { s ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        SummaryStat("Sets", s.totalSets.toString())
                                        SummaryStat("Volume", "${s.totalVolume.toInt()}kg")
                                        SummaryStat("Time", "${s.duration / 60000}m")
                                    }
                                }
                            }
                        }

                        s.exercises.forEach { (exercise, sets) ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            exercise,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        sets.forEach { set ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Set ${set.setNumber}", style = MaterialTheme.typography.bodyLarge)
                                                Text(
                                                    "${set.weight}kg × ${set.reps}",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("DONE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Hidden view for capturing
            summary?.let { s ->
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
fun ShareableSummary(summary: SessionSummary) {
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
        Text("Date: ${sdf.format(Date(summary.date))}", color = Color.Black)
        Text("Body Weight: ${summary.bodyWeight ?: "-"} kg", color = Color.Black)

        Spacer(Modifier.height(16.dp))

        summary.exercises.forEach { (exercise, sets) ->
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
            "Total Volume: ${summary.totalVolume.toInt()} kg",
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

fun buildShareText(summary: SessionSummary): String {
    val sb = StringBuilder()
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    sb.append("Owl Fitness Workout Summary\n")
    sb.append("---------------------------\n")
    sb.append("Date: ${sdf.format(Date(summary.date))}\n")
    
    summary.bodyWeight?.let {
        sb.append("Body Weight: $it kg\n")
    }
    
    sb.append("Duration: ${summary.duration / 60000} min\n\n")

    summary.exercises.forEach { (exercise, sets) ->
        sb.append("$exercise\n")
        sets.forEach {
            sb.append("- Set ${it.setNumber}: ${it.weight}kg x ${it.reps}\n")
        }
        sb.append("\n")
    }

    sb.append("---------------------------\n")
    sb.append("Total Volume: ${summary.totalVolume.toInt()} kg\n")
    sb.append("Total Sets: ${summary.totalSets}\n")
    
    return sb.toString()
}

@Composable
fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}
