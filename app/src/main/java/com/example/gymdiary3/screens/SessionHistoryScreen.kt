package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.components.EmptyState
import com.example.gymdiary3.ui.components.appear
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SessionHistoryScreen(
    nav: NavHostController,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val sessionsWithSets by viewModel.sessionsWithSets.collectAsStateWithLifecycle()
    val userSettings by viewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    val sdf = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) }
    val timeSdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = Apex.Surface2,
            title = { Text("Delete session", color = Apex.TextPrimary) },
            text = { Text("Are you sure you want to delete this workout session?", color = Apex.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteSession(it) }
                    showDeleteDialog = null
                }) { Text("DELETE", color = Apex.Negative) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("CANCEL", color = Apex.TextSecondary)
                }
            }
        )
    }

    LaunchedEffect(Unit) { viewModel.deleteEmptySessions() }

    ApexScaffold(title = "Session History", onBack = { nav.popBackStack() }) { padding ->
        if (sessionsWithSets.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    message = "Your completed sessions will appear here.",
                    title = "NO WORKOUT HISTORY"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(sessionsWithSets, key = { _, s -> s.session.id }) { index, s ->
                    SessionCard(s, Modifier.appear(index), sdf, timeSdf, userSettings.weightUnit,
                        onOpen = { nav.navigate("summary/${s.session.id}") },
                        onDelete = { showDeleteDialog = s.session.id }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    s: SessionWithSets,
    modifier: Modifier,
    sdf: SimpleDateFormat,
    timeSdf: SimpleDateFormat,
    unit: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val session = s.session
    ApexPanel(modifier = modifier, onClick = onOpen, onLongClick = onDelete, radius = Apex.radiusMd) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    sdf.format(Date(session.startTime)),
                    style = MaterialTheme.typography.titleMedium,
                    color = Apex.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                val duration = s.duration / 60000
                if (duration > 0) {
                    Text("$duration min", style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted)
                }
            }

            val muscleGroups = remember(s.sets) {
                s.sets.map { it.muscle }.distinct().sorted().joinToString(" · ")
            }
            if (muscleGroups.isNotEmpty()) {
                Text(
                    muscleGroups,
                    style = MaterialTheme.typography.labelSmall,
                    color = Apex.AccentSoft,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(
                        timeSdf.format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Apex.AccentSoft,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${WeightFormatter.formatFromKilograms(s.totalVolume, unit, decimals = 0)} total",
                        style = MaterialTheme.typography.labelSmall,
                        color = Apex.TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    "VIEW SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Apex.AccentSoft,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
