package com.example.gymdiary3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.ProgramDay
import com.example.gymdiary3.domain.model.SessionSchedule
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.viewmodel.ProgramViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class ProgramActions(
    val onBack: () -> Unit = {},
    val onPlanToday: (ProgramDay) -> Unit = {},
    val onStart: (SessionSchedule) -> Unit = {}
)

private const val PLANNED = "Planned"
private const val DONE = "Done"

/** "70 min" with a non-breaking space so the unit never wraps away from its number. */
private fun minutes(m: Int) = "$m min"

@Composable
fun ProgramTrackerScreen(nav: NavHostController, viewModel: ProgramViewModel = hiltViewModel()) {
    val days by viewModel.allProgramDays.collectAsStateWithLifecycle()
    val sessions by viewModel.scheduledSessions.collectAsStateWithLifecycle()
    ProgramsContent(
        days, sessions, System.currentTimeMillis(),
        ProgramActions(
            onBack = { nav.navigateUp() },
            onPlanToday = { viewModel.scheduleSession(it, System.currentTimeMillis()) },
            onStart = { s -> viewModel.logScheduledSession(s) { id -> nav.navigate("program_log/$id") } }
        )
    )
}

@Composable
fun ProgramsContent(
    days: List<ProgramDay>,
    sessions: List<SessionSchedule>,
    now: Long,
    actions: ProgramActions,
    modifier: Modifier = Modifier,
    zone: ZoneId = ZoneId.systemDefault()
) {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val week = (0L..6L).map { monday.plusDays(it) }
    fun dateOf(s: SessionSchedule): LocalDate = Instant.ofEpochMilli(s.date).atZone(zone).toLocalDate()
    val thisWeek = sessions.filter { dateOf(it) in week }
    val upNext = thisWeek.filter { it.status == PLANNED }.sortedBy { it.date }
    val plannedToday = sessions.filter { it.status == PLANNED && dateOf(it) == today }.mapNotNull { it.programDayId }.toSet()

    Column(modifier.fillMaxSize()) {
        DetailTopBar("Programs", onBack = actions.onBack)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = Gd.s8)) {
            item(key = "week") {
                SectionHeader("This week", top = Gd.s3)
                ProgramWeek(week, today, thisWeek, ::dateOf, actions.onStart, Modifier.gutter().padding(top = Gd.s2))
            }

            if (upNext.isNotEmpty()) {
                item(key = "upNextHeader") { SectionHeader("Up next") }
                itemsIndexed(upNext, key = { _, s -> "next_${s.id}" }) { i, s ->
                    val d = dateOf(s)
                    val programDay = days.firstOrNull { it.id == s.programDayId }
                    ListRow(
                        title = programDay?.sessionType ?: s.title,
                        subtitle = listOfNotNull(
                            if (d == today) "Today" else d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            programDay?.plannedDuration?.takeIf { it > 0 }?.let(::minutes)
                        ).joinToString(" · "),
                        titleStrong = true,
                        trailing = { TextAction("Start", onClick = { actions.onStart(s) }, color = Gd.AccentText) },
                        onClick = { actions.onStart(s) }
                    )
                    if (i < upNext.lastIndex) Hairline()
                }
            }

            if (days.isNotEmpty()) {
                item(key = "programHeader") { SectionHeader("Program") }
                itemsIndexed(days, key = { _, d -> "day_${d.id}" }) { i, day ->
                    val rest = day.sessionType.equals("Rest", ignoreCase = true)
                    ListRow(
                        title = day.sessionType,
                        overline = "Day ${day.dayNumber}",
                        subtitle = listOfNotNull(
                            day.primaryPriority,
                            day.plannedDuration?.takeIf { it > 0 && !rest }?.let(::minutes)
                        ).joinToString(" · ").ifEmpty { null },
                        trailing = {
                            when {
                                rest -> {}
                                // Same inset as TextAction so the two right edges line up.
                                day.id in plannedToday -> Text(
                                    "Planned", style = GdType.label, color = Gd.TextMuted,
                                    modifier = Modifier.padding(horizontal = Gd.s3)
                                )
                                else -> TextAction("Plan today", onClick = { actions.onPlanToday(day) })
                            }
                        }
                    )
                    if (i < days.lastIndex) Hairline()
                }
            } else {
                item(key = "empty") { EmptyMessage("No program yet", "The default push/pull/legs program is being set up.") }
            }
        }
    }
}

/** Monday–Sunday. Done days fill, planned days show a ring; tapping a planned day starts it. */
@Composable
private fun ProgramWeek(
    week: List<LocalDate>,
    today: LocalDate,
    sessions: List<SessionSchedule>,
    dateOf: (SessionSchedule) -> LocalDate,
    onStart: (SessionSchedule) -> Unit,
    modifier: Modifier = Modifier
) {
    val done = sessions.count { it.status == DONE }
    Row(
        modifier.fillMaxWidth().semantics { contentDescription = "$done sessions done this week" },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        week.forEach { date ->
            val daySessions = sessions.filter { dateOf(it) == date }
            val planned = daySessions.firstOrNull { it.status == PLANNED }
            val isDone = daySessions.any { it.status == DONE }
            val isToday = date == today
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(40.dp)
                    .clickable(enabled = planned != null) { planned?.let(onStart) }
                    .padding(vertical = Gd.s1)
            ) {
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = GdType.meta.copy(fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium),
                    color = if (isToday) Gd.Text else Gd.TextFaint
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    date.dayOfMonth.toString(),
                    style = GdType.labelNum.copy(fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal),
                    color = if (isToday) Gd.Text else Gd.TextMuted
                )
                Spacer(Modifier.height(Gd.s2))
                Box(
                    Modifier
                        .size(10.dp)
                        .then(
                            when {
                                isDone -> Modifier.background(Gd.Text, CircleShape)
                                planned != null -> Modifier.border(1.5.dp, Gd.TextMuted, CircleShape)
                                else -> Modifier.border(1.5.dp, Gd.Border, CircleShape)
                            }
                        )
                )
            }
        }
    }
}
