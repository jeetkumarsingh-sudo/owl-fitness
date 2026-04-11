package com.example.gymdiary3.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.components.EmptyState
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SessionHistoryScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    val sessionsWithSets by viewModel.sessionsWithSets.collectAsStateWithLifecycle()
    val sdf = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) }
    val timeSdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = OwlColors.CardBg,
            title = { Text("Delete Session", color = OwlColors.TextPrimary) },
            text = { Text("Are you sure you want to delete this workout session?", color = OwlColors.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteSession(it) }
                    showDeleteDialog = null
                }) {
                    Text("DELETE", color = OwlColors.RedNegative)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("CANCEL", color = OwlColors.TextSecondary)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.deleteEmptySessions()
    }

    Scaffold(
        containerColor = OwlColors.DeepBg,
        topBar = { 
            TopAppBar(
                title = { Text("SESSION HISTORY", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwlColors.DeepBg,
                    titleContentColor = OwlColors.TextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OwlColors.TextPrimary)
                    }
                }
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (sessionsWithSets.isEmpty()) {
                EmptyState(
                    message = "Your completed sessions will appear here.",
                    title = "NO WORKOUT HISTORY"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = sessionsWithSets,
                        key = { it.session.id }
                    ) { sessionWithSets ->
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
                        ) {
                            SessionCard(sessionWithSets, nav, sdf, timeSdf) {
                                showDeleteDialog = it
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionCard(
    sessionWithSets: com.example.gymdiary3.data.SessionWithSets,
    nav: NavHostController,
    sdf: SimpleDateFormat,
    timeSdf: SimpleDateFormat,
    onLongClick: (Int) -> Unit
) {
    val session = sessionWithSets.session
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .combinedClickable(
                onClick = { 
                    scope.launch {
                        scale.animateTo(0.95f, tween(100))
                        scale.animateTo(1f, tween(100))
                    }
                    nav.navigate("summary/${session.id}") 
                },
                onLongClick = { onLongClick(session.id) }
            ),
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sdf.format(Date(session.startTime)),
                    style = MaterialTheme.typography.titleMedium,
                    color = OwlColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                val duration = sessionWithSets.duration / 60000
                if (duration > 0) {
                    Text(
                        text = "$duration min",
                        style = MaterialTheme.typography.labelSmall,
                        color = OwlColors.TextMuted
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(
                        text = timeSdf.format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OwlColors.Purple,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${sessionWithSets.totalVolume.toInt()} kg total",
                        style = MaterialTheme.typography.labelSmall,
                        color = OwlColors.TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Text(
                    text = "VIEW SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = OwlColors.Purple,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
