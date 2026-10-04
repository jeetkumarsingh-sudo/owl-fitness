package com.example.gymdiary3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.ProgramDay
import com.example.gymdiary3.domain.model.SessionSchedule
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.components.SectionLabel
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.ProgramViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgramTrackerScreen(
    nav: NavHostController,
    viewModel: ProgramViewModel = hiltViewModel()
) {
    val programDays by viewModel.allProgramDays.collectAsStateWithLifecycle()
    val scheduledSessions by viewModel.scheduledSessions.collectAsStateWithLifecycle()

    ApexScaffold(title = "Gym Tracker", onBack = { nav.navigateUp() }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            item {
                SectionLabel("This week")
                Spacer(Modifier.height(12.dp))
                WeeklyCalendarView(scheduledSessions) { session ->
                    if (session.status == "Planned") {
                        viewModel.logScheduledSession(session) { id -> nav.navigate("program_log/$id") }
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                SectionLabel("Program days")
            }
            items(programDays) { day ->
                ProgramDayCard(day) { viewModel.scheduleSession(day, System.currentTimeMillis()) }
            }
        }
    }
}

@Composable
private fun WeeklyCalendarView(
    sessions: List<SessionSchedule>,
    onSessionClick: (SessionSchedule) -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dayOfMonthFormat = SimpleDateFormat("d", Locale.getDefault())
    val today = Calendar.getInstance()
    val days = (0..6).map { i ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.add(Calendar.DAY_OF_YEAR, i)
        cal.time
    }

    ApexPanel(radius = Apex.radiusMd) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { date ->
                val isToday = isSameDay(date, today.time)
                val session = sessions.find { isSameDay(Date(it.date), date) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(enabled = session != null) { session?.let { onSessionClick(it) } }
                        .padding(4.dp)
                ) {
                    Text(
                        dateFormat.format(date).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) Apex.AccentSoft else Apex.TextMuted
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            session?.status == "Done" -> Apex.Positive.copy(alpha = 0.2f)
                            session?.status == "Planned" -> Apex.AccentWash
                            isToday -> Apex.Accent
                            else -> Color.Transparent
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                dayOfMonthFormat.format(date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isToday && session == null) Color.White else Apex.TextPrimary,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    if (session != null) {
                        Icon(
                            imageVector = if (session.status == "Done") Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            tint = if (session.status == "Done") Apex.Positive else Apex.AccentSoft,
                            modifier = Modifier.size(8.dp).padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramDayCard(day: ProgramDay, onClick: () -> Unit) {
    ApexPanel(onClick = onClick, radius = Apex.radiusMd) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(day.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Apex.TextPrimary)
                Text("${day.sessionType} · ${day.plannedDuration} min", style = MaterialTheme.typography.bodySmall, color = Apex.TextSecondary)
            }
            Box(
                Modifier.size(36.dp).background(Apex.AccentWash, RoundedCornerShape(100)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Schedule", tint = Apex.AccentSoft, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
