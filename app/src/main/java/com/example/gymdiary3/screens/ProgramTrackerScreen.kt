package com.example.gymdiary3.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.ProgramDay
import com.example.gymdiary3.domain.model.SessionSchedule
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.ProgramViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramTrackerScreen(
    nav: NavHostController,
    viewModel: ProgramViewModel = hiltViewModel()
) {
    val programDays by viewModel.allProgramDays.collectAsStateWithLifecycle()
    val scheduledSessions by viewModel.scheduledSessions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GYM TRACKER", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwlColors.DeepBg,
                    titleContentColor = OwlColors.TextPrimary,
                    navigationIconContentColor = OwlColors.TextPrimary
                )
            )
        },
        containerColor = OwlColors.DeepBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader("THIS WEEK")
                WeeklyCalendarView(scheduledSessions) { session ->
                    // Handle session click
                    if (session.status == "Planned") {
                        viewModel.logScheduledSession(session) { id ->
                            nav.navigate("program_log/$id")
                        }
                    }
                }
            }

            item {
                SectionHeader("PROGRAM DAYS")
            }

            items(programDays) { day ->
                ProgramDayCard(day) {
                    // Logic to schedule or quick log
                    viewModel.scheduleSession(day, System.currentTimeMillis())
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = OwlColors.TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun WeeklyCalendarView(
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
                        text = dateFormat.format(date).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) OwlColors.Purple else OwlColors.TextMuted
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            session?.status == "Done" -> OwlColors.GreenBulk.copy(alpha = 0.2f)
                            session?.status == "Planned" -> OwlColors.Purple.copy(alpha = 0.2f)
                            isToday -> OwlColors.Purple
                            else -> Color.Transparent
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = dayOfMonthFormat.format(date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isToday && session == null) Color.White else OwlColors.TextPrimary,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    if (session != null) {
                        Icon(
                            imageVector = if (session.status == "Done") Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            tint = if (session.status == "Done") OwlColors.GreenBulk else OwlColors.Purple,
                            modifier = Modifier.size(8.dp).padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramDayCard(day: ProgramDay, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(day.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OwlColors.TextPrimary)
                Text(
                    "${day.sessionType} · ${day.plannedDuration} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = OwlColors.TextSecondary
                )
            }
            Icon(Icons.Default.Add, contentDescription = "Schedule", tint = OwlColors.Purple)
        }
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
