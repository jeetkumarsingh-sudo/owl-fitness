package com.example.gymdiary3.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.presentation.history.HistoryMonth
import com.example.gymdiary3.presentation.history.HistoryUiState
import com.example.gymdiary3.presentation.history.RestEntry
import com.example.gymdiary3.presentation.history.SessionEntry
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.viewmodel.HistoryViewModel

data class HistoryActions(
    val onOpen: (Int) -> Unit = {},
    val onDelete: (Int) -> Unit = {},
    val onStart: () -> Unit = {}
)

@Composable
fun SessionHistoryScreen(nav: NavHostController, vm: HistoryViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val s = state ?: return
    HistoryScreen(
        s,
        HistoryActions(
            onOpen = { nav.navigate("summary/$it") },
            onDelete = vm::delete,
            onStart = { nav.navigate("home") { launchSingleTop = true } }
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(state: HistoryUiState, actions: HistoryActions, modifier: Modifier = Modifier) {
    var deleting by remember { mutableStateOf<SessionEntry?>(null) }

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Gd.s8)) {
        item(key = "header") { ScreenHeader("History", subtitle = if (state.isEmpty) null else state.subtitle) }

        if (state.isEmpty) {
            item(key = "empty") {
                EmptyMessage(
                    title = "No workouts yet",
                    body = "Finished workouts appear here, grouped by month.",
                    action = { SecondaryButton("Start a workout", onClick = actions.onStart, compact = true) }
                )
            }
        }

        state.months.forEach { month ->
            stickyHeader(key = "m_${month.key}") { MonthHeader(month) }
            items(month.entries, key = { it.key }) { entry ->
                when (entry) {
                    is SessionEntry -> SessionRow(
                        entry,
                        onClick = { actions.onOpen(entry.sessionId) },
                        onLongClick = { deleting = entry },
                        modifier = itemMotion()
                    )
                    is RestEntry -> RestRow(entry, itemMotion())
                }
            }
        }
    }

    deleting?.let { e ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = Gd.SurfaceRaised,
            title = { Text("Delete this workout?", style = GdType.section, color = Gd.Text) },
            text = { Text("${e.title} on ${e.weekday} ${e.day} and all its sets will be removed.", style = GdType.body, color = Gd.TextMuted) },
            confirmButton = { TextAction("Delete", onClick = { actions.onDelete(e.sessionId); deleting = null }, color = Gd.Danger) },
            dismissButton = { TextAction("Cancel", onClick = { deleting = null }) }
        )
    }
}

@Composable
private fun MonthHeader(month: HistoryMonth) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Gd.Bg)
            .gutter()
            .padding(top = Gd.s6, bottom = Gd.s2),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(month.title, style = GdType.section, color = Gd.Text, modifier = Modifier.weight(1f))
        Text(month.summary, style = GdType.labelNum, color = Gd.TextMuted)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionRow(entry: SessionEntry, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .gutter()
            .padding(vertical = Gd.s3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(48.dp)) {
            Text(entry.day, style = GdType.metric, color = Gd.Text)
            Text(entry.weekday, style = GdType.meta, color = Gd.TextMuted)
        }
        Spacer(Modifier.width(Gd.s3))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.title, style = GdType.bodyStrong, color = Gd.Text)
                if (entry.hasPr) {
                    Spacer(Modifier.width(Gd.s2))
                    StatusLabel("PR", Gd.AccentText)
                }
            }
            Text(entry.detail, style = GdType.labelNum, color = Gd.TextMuted)
        }
        Chevron()
    }
}

@Composable
private fun RestRow(entry: RestEntry, modifier: Modifier = Modifier) {
    Text(
        entry.label,
        style = GdType.meta,
        color = Gd.TextFaint,
        modifier = modifier.fillMaxWidth().padding(start = Gd.Gutter + 60.dp, top = 2.dp, bottom = 2.dp)
    )
}
