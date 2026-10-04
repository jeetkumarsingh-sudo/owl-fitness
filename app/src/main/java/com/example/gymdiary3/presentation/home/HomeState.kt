package com.example.gymdiary3.presentation.home

import com.example.gymdiary3.domain.history.SessionSplit
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.progression.PrDetector
import com.example.gymdiary3.domain.progression.ProgressionEngine
import com.example.gymdiary3.domain.recovery.RecoveryEngine
import com.example.gymdiary3.domain.recovery.TodayRecommendation
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.presentation.common.TrainingCalendar
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.presentation.insight.InsightPresenter
import com.example.gymdiary3.presentation.insight.InsightRow
import com.example.gymdiary3.ui.design.WeekDay

/**
 * Home answers three questions and nothing else:
 *  1. What should I do today?   → [active] or [today]
 *  2. How am I progressing?     → [week], [workoutsThisWeek], [streakDays], [recentPr], [lastWorkout]
 *  3. Is there anything to know → [insight] (one)
 */
data class HomeUiState(
    val dateLine: String,
    val greeting: String,
    val active: ActiveSession?,
    val today: TodayRecommendation,
    val week: List<WeekDay>,
    val workoutsThisWeek: Int,
    val streakDays: Int,
    val recentPr: PrLine?,
    val lastWorkout: LastWorkoutLine?,
    val insight: InsightRow?,
    val isNewUser: Boolean
)

data class ActiveSession(val exerciseCount: Int, val setCount: Int)
data class PrLine(val exercise: String, val value: String, val whenLabel: String)
data class LastWorkoutLine(val sessionId: Int, val title: String, val subtitle: String)

object HomeStateBuilder {

    /** Insights below this priority (e.g. "stable") are not worth Home's one slot. */
    private const val HOME_INSIGHT_MIN_PRIORITY = 30

    fun build(
        sessions: List<SessionWithSets>,
        activeSessionId: Int?,
        engineInsights: List<FitnessInsight>,
        unit: String,
        now: Long
    ): HomeUiState {
        val starts = sessions.map { it.session.startTime }
        val allSets = sessions.flatMap { it.sets }

        val active = activeSessionId?.let { id ->
            val s = sessions.firstOrNull { it.session.id == id }
            ActiveSession(
                exerciseCount = s?.sets?.map { it.exercise }?.distinct()?.size ?: 0,
                setCount = s?.sets?.size ?: 0
            )
        }

        val last7 = starts.count { it >= now - 7 * TrainingCalendar.DAY_MS }
        val today = RecoveryEngine.today(RecoveryEngine.analyze(allSets, now), last7, now)

        val weekStart = TrainingCalendar.startOfWeek(now)
        val pr = PrDetector.events(allSets).lastOrNull()?.let {
            PrLine(it.exercise, Fmt.weightUnit(it.weightKg, unit), TrainingCalendar.relativeDay(it.date, now))
        }

        val finished = sessions.filter { it.session.id != activeSessionId }
        val last = finished.maxByOrNull { it.session.startTime }?.let {
            LastWorkoutLine(
                sessionId = it.session.id,
                title = SessionSplit.label(it.sets),
                subtitle = listOf(
                    TrainingCalendar.relativeDay(it.session.startTime, now),
                    "${it.sets.size} sets",
                    Fmt.duration(it.duration)
                ).joinToString(" · ")
            )
        }

        val progressions = allSets.groupBy { it.exercise }
            .map { (name, sets) -> ProgressionEngine.analyze(name, sets, unit) }
        val insight = InsightPresenter.build(progressions, engineInsights, unit, now)
            .firstOrNull { it.priority >= HOME_INSIGHT_MIN_PRIORITY }

        return HomeUiState(
            dateLine = TrainingCalendar.dateLine(now),
            greeting = TrainingCalendar.greeting(now),
            active = active,
            today = today,
            week = TrainingCalendar.week(starts, now),
            workoutsThisWeek = starts.count { it >= weekStart },
            streakDays = TrainingCalendar.streak(starts, now),
            recentPr = pr,
            lastWorkout = last,
            insight = insight,
            isNewUser = sessions.isEmpty()
        )
    }
}
