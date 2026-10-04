package com.example.gymdiary3.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.settings.UserSettings
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.ui.components.*
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import com.example.gymdiary3.viewmodel.ProgressViewModel
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    nav: NavHostController,
    viewModel: WorkoutViewModel = hiltViewModel(),
    bodyViewModel: BodyWeightViewModel = hiltViewModel(),
    progressViewModel: ProgressViewModel = hiltViewModel()
) {
    val currentSessionId by viewModel.sessionManager.currentSessionId.collectAsStateWithLifecycle()
    val latestWeight by bodyViewModel.latestBodyWeight.collectAsStateWithLifecycle()
    val totalWorkouts by viewModel.totalWorkoutCount.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val exerciseUiStates by viewModel.exerciseUiStates.collectAsStateWithLifecycle()
    val insights by progressViewModel.fitnessInsights.collectAsStateWithLifecycle()
    val userSettings by viewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(UserSettings())

    val sessionDuration by viewModel.sessionDurationSeconds.collectAsStateWithLifecycle()
    val exercisesThisSession by viewModel.exercisesThisSession.collectAsStateWithLifecycle()

    // Derived training status
    val streak = remember(sessions) { computeStreak(sessions) }
    val week = remember(sessions) { computeWeek(sessions) }
    val totalVolumeKg = remember(sessions) { sessions.sumOf { it.totalVolume } }
    val prCount = remember(exerciseUiStates) { exerciseUiStates.values.count { it.isPR } }
    val lastSession = remember(sessions) { sessions.maxByOrNull { it.date } }

    var showSessionDateDialog by remember { mutableStateOf(false) }

    if (showSessionDateDialog) {
        StartWorkoutDialog(
            onStartNow = {
                showSessionDateDialog = false
                viewModel.startSession(System.currentTimeMillis())
            },
            onLogYesterday = {
                showSessionDateDialog = false
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
                viewModel.startSession(cal.timeInMillis)
            },
            onDismiss = { showSessionDateDialog = false }
        )
    }

    ApexBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = Apex.space6)
        ) {
            Spacer(Modifier.height(Apex.space6))
            DashboardHeader(onSettings = { nav.navigate("settings") })

            Spacer(Modifier.height(Apex.space6))

            if (currentSessionId != null) {
                ActiveSessionHero(
                    durationSeconds = sessionDuration,
                    exerciseCount = exercisesThisSession.size,
                    onResume = { nav.navigate("muscle") },
                    onFinish = { viewModel.endSession { nav.navigate("history") } }
                )
            } else {
                StatusHero(
                    streak = streak,
                    week = week,
                    onStart = { showSessionDateDialog = true },
                    onProgram = { nav.navigate("program_tracker") }
                )
            }

            Spacer(Modifier.height(Apex.space6))
            SectionLabel("Lifetime")
            Spacer(Modifier.height(Apex.space4))
            QuickStats(
                workouts = totalWorkouts,
                totalVolumeKg = totalVolumeKg,
                prCount = prCount,
                unit = userSettings.weightUnit,
                onWorkouts = { nav.navigate("history") },
                onPrs = { nav.navigate("progress") }
            )

            Spacer(Modifier.height(Apex.space4))
            BodyAndLastRow(
                latestWeightKg = latestWeight?.weight,
                unit = userSettings.weightUnit,
                lastSession = lastSession,
                onWeight = { nav.navigate("weight") },
                onLast = { lastSession?.let { nav.navigate("summary/${it.session.id}") } }
            )

            val topInsights = insights.take(2)
            if (topInsights.isNotEmpty()) {
                Spacer(Modifier.height(Apex.space6))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Intelligence", accent = true)
                    Text(
                        "VIEW ALL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Apex.TextMuted,
                        modifier = Modifier.clickable { nav.navigate("progress") }
                    )
                }
                Spacer(Modifier.height(Apex.space4))
                topInsights.forEach { insight ->
                    InsightPeek(insight)
                    Spacer(Modifier.height(Apex.space3))
                }
            }

            Spacer(Modifier.height(Apex.space10))
        }
    }
}

/* -------------------------------------------------------------------------- */

@Composable
private fun DashboardHeader(onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "APEX FITNESS",
                style = MaterialTheme.typography.labelMedium,
                color = Apex.AccentSoft,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                greeting(),
                style = MaterialTheme.typography.headlineLarge,
                color = Apex.TextPrimary
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Apex.Surface2, CircleShape)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Apex.TextSecondary)
        }
    }
}

@Composable
private fun StatusHero(
    streak: Int,
    week: WeekActivity,
    onStart: () -> Unit,
    onProgram: () -> Unit
) {
    ApexPanel(accentEdge = true) {
        Column(Modifier.padding(Apex.space6)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (streak > 0) Apex.AccentBright else Apex.TextMuted,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(Apex.space3))
                Row(verticalAlignment = Alignment.Bottom) {
                    CountUpText(
                        target = streak,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp),
                        color = Apex.TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "DAY${if (streak == 1) "" else "S"}\nSTREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Apex.TextSecondary,
                        letterSpacing = 1.sp,
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "${week.activeCount} / 7\nTHIS WEEK",
                    style = MaterialTheme.typography.labelSmall,
                    color = Apex.TextMuted,
                    textAlign = TextAlign.End,
                    lineHeight = 14.sp
                )
            }

            Spacer(Modifier.height(Apex.space5))
            WeekActivityBars(
                activeDays = week.activeDays,
                dayInitials = week.initials,
                todayIndex = week.todayIndex
            )

            Spacer(Modifier.height(Apex.space6))
            ApexCta(
                text = "START WORKOUT",
                onClick = onStart,
                leading = {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            )
            Spacer(Modifier.height(Apex.space3))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Apex.radiusSm))
                    .clickable(onClick = onProgram)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Follow a program",
                    style = MaterialTheme.typography.labelLarge,
                    color = Apex.TextSecondary
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Apex.TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ActiveSessionHero(
    durationSeconds: Long,
    exerciseCount: Int,
    onResume: () -> Unit,
    onFinish: () -> Unit
) {
    ApexPanel(accentEdge = true) {
        Column(Modifier.padding(Apex.space6)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Apex.Positive, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(
                    "WORKOUT IN PROGRESS",
                    style = MaterialTheme.typography.labelMedium,
                    color = Apex.Positive,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(Apex.space6))
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "%02d:%02d".format(durationSeconds / 60, durationSeconds % 60),
                        style = MaterialTheme.typography.displaySmall,
                        color = Apex.TextPrimary,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("DURATION", style = MaterialTheme.typography.labelSmall, color = Apex.TextSecondary, letterSpacing = 1.sp)
                }
                Column(Modifier.weight(1f)) {
                    CountUpText(
                        target = exerciseCount,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Light),
                        color = Apex.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("EXERCISES", style = MaterialTheme.typography.labelSmall, color = Apex.TextSecondary, letterSpacing = 1.sp)
                }
            }
            Spacer(Modifier.height(Apex.space8))
            ApexCta(text = "RESUME WORKOUT", onClick = onResume)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
                Text("FINISH SESSION", color = Apex.TextSecondary, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun QuickStats(
    workouts: Int,
    totalVolumeKg: Double,
    prCount: Int,
    unit: String,
    onWorkouts: () -> Unit,
    onPrs: () -> Unit
) {
    val volumeDisplay = WeightFormatter.fromKilograms(totalVolumeKg, unit).toInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Apex.space4)
    ) {
        ApexPanel(modifier = Modifier.weight(1f), onClick = onWorkouts, radius = Apex.radiusMd) {
            StatValue(value = workouts, label = "Workouts", modifier = Modifier.padding(Apex.space5))
        }
        ApexPanel(modifier = Modifier.weight(1.3f), radius = Apex.radiusMd) {
            Column(Modifier.padding(Apex.space5)) {
                CountUpText(
                    target = volumeDisplay,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Apex.AccentSoft,
                    suffix = " ${WeightFormatter.label(unit)}"
                )
                Spacer(Modifier.height(4.dp))
                Text("TOTAL VOLUME", style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted, letterSpacing = 1.sp)
            }
        }
        ApexPanel(modifier = Modifier.weight(1f), onClick = onPrs, radius = Apex.radiusMd) {
            Column(Modifier.padding(Apex.space5)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CountUpText(
                        target = prCount,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Apex.TextPrimary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Apex.Warning, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text("PRS", style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun BodyAndLastRow(
    latestWeightKg: Double?,
    unit: String,
    lastSession: SessionWithSets?,
    onWeight: () -> Unit,
    onLast: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Apex.space4)
    ) {
        ApexPanel(modifier = Modifier.weight(1f), onClick = onWeight, radius = Apex.radiusMd) {
            Column(Modifier.padding(Apex.space5)) {
                Text("BODY WEIGHT", style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted, letterSpacing = 1.sp)
                Spacer(Modifier.height(Apex.space3))
                if (latestWeightKg != null) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        CountUpDecimalText(
                            target = WeightFormatter.fromKilograms(latestWeightKg, unit).toFloat(),
                            decimals = 1,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Apex.TextPrimary
                        )
                        Text(
                            " ${WeightFormatter.label(unit)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Apex.TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                } else {
                    Text("—", style = MaterialTheme.typography.headlineMedium, color = Apex.TextMuted)
                }
            }
        }

        ApexPanel(modifier = Modifier.weight(1f), onClick = onLast, radius = Apex.radiusMd) {
            Column(Modifier.padding(Apex.space5)) {
                Text("LAST WORKOUT", style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted, letterSpacing = 1.sp)
                Spacer(Modifier.height(Apex.space3))
                if (lastSession != null) {
                    val sdf = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
                    Text(
                        sdf.format(Date(lastSession.date)),
                        style = MaterialTheme.typography.titleMedium,
                        color = Apex.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${lastSession.sets.size} sets · ${lastSession.duration / 60000}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = Apex.TextSecondary
                    )
                } else {
                    Text("—", style = MaterialTheme.typography.headlineMedium, color = Apex.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun InsightPeek(insight: FitnessInsight) {
    val accent = when (insight.severity) {
        InsightSeverity.POSITIVE -> Apex.Positive
        InsightSeverity.WARNING -> Apex.Warning
        InsightSeverity.ACTION_REQUIRED -> Apex.Negative
        InsightSeverity.INFO -> Apex.Info
    }
    ApexPanel(radius = Apex.radiusMd) {
        Row(Modifier.padding(Apex.space4), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(accent, RoundedCornerShape(100))
            )
            Spacer(Modifier.width(Apex.space3))
            Column {
                insight.exerciseName?.let {
                    Text(it.uppercase(), style = MaterialTheme.typography.labelSmall, color = accent, letterSpacing = 1.sp)
                    Spacer(Modifier.height(2.dp))
                }
                Text(insight.message, style = MaterialTheme.typography.bodySmall, color = Apex.TextPrimary)
            }
        }
    }
}

@Composable
private fun StartWorkoutDialog(
    onStartNow: () -> Unit,
    onLogYesterday: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Apex.Surface2,
        title = {
            Text(
                "START EMPTY WORKOUT",
                style = MaterialTheme.typography.titleMedium,
                color = Apex.TextPrimary,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onStartNow),
                    color = Apex.Accent,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Now", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, color = Color.White, textAlign = TextAlign.Center)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onLogYesterday),
                    color = Apex.Surface3,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Apex.Hairline)
                ) {
                    Text("Log Yesterday", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, color = Apex.TextSecondary, textAlign = TextAlign.Center)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Apex.TextMuted, letterSpacing = 1.sp)
            }
        }
    )
}

/* ------------------------------ derived data ------------------------------ */

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Good morning."
        h < 18 -> "Ready to work."
        else -> "Evening grind."
    }
}

private const val DAY_MS = 24L * 60 * 60 * 1000

private fun startOfDay(millis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun computeStreak(sessions: List<SessionWithSets>): Int {
    if (sessions.isEmpty()) return 0
    val days = sessions.map { startOfDay(it.date) }.toHashSet()
    var cursor = startOfDay(System.currentTimeMillis())
    // Allow the streak to stand if today has no workout yet.
    if (cursor !in days) cursor -= DAY_MS
    var streak = 0
    while (cursor in days) {
        streak++
        cursor -= DAY_MS
    }
    return streak
}

data class WeekActivity(
    val activeDays: Set<Int>,
    val initials: List<String>,
    val todayIndex: Int,
    val activeCount: Int
)

private fun computeWeek(sessions: List<SessionWithSets>): WeekActivity {
    val cal = Calendar.getInstance()
    // Start of the current week at 00:00
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val firstDow = cal.firstDayOfWeek
    while (cal.get(Calendar.DAY_OF_WEEK) != firstDow) cal.add(Calendar.DAY_OF_MONTH, -1)
    val weekStart = cal.timeInMillis

    val initialsFmt = SimpleDateFormat("EEE", Locale.getDefault())
    val dayStarts = (0..6).map { weekStart + it * DAY_MS }
    val initials = dayStarts.map { initialsFmt.format(Date(it)).take(1).uppercase() }

    val today = startOfDay(System.currentTimeMillis())
    val todayIndex = dayStarts.indexOfFirst { it == today }

    val sessionDays = sessions.map { startOfDay(it.date) }.toHashSet()
    val active = dayStarts.mapIndexedNotNull { i, d -> if (d in sessionDays) i else null }.toSet()

    return WeekActivity(active, initials, todayIndex, active.size)
}
