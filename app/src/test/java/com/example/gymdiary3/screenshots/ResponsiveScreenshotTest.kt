package com.example.gymdiary3.screenshots

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.domain.analytics.TimeRange
import com.example.gymdiary3.domain.model.SessionExerciseLog
import com.example.gymdiary3.presentation.body.BodyStateBuilder
import com.example.gymdiary3.presentation.exercise.ExerciseDetailStateBuilder
import com.example.gymdiary3.presentation.exercise.ExerciseTab
import com.example.gymdiary3.presentation.home.HomeStateBuilder
import com.example.gymdiary3.presentation.workout.ActiveWorkoutStateBuilder
import com.example.gymdiary3.presentation.workout.LoggerStateBuilder
import com.example.gymdiary3.screens.*
import com.example.gymdiary3.viewmodel.RestUi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The same screens at the edges of the size range: a small phone with the
 * system font at 130% (truncation, wrapping, touch targets) and a Medium-width
 * window (tablet portrait, phone landscape) where content must not stretch.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_SMALL, application = Application::class)
class ResponsiveScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val large = 1.3f
    private val all = SampleData.sessionsWithActive

    @Test fun homeSmallLargeText() = compose.snap("R_small_home", large) {
        HomeScreen(HomeStateBuilder.build(SampleData.sessions, null, emptyList(), "kg", SampleData.now), 0, HomeActions())
    }

    @Test fun loggerSmallLargeText() = compose.snap("R_small_logger", large) {
        val sets = all.flatMap { it.sets }.filter { it.exercise == "Lat Pulldown" }
        LoggerScreen(
            LoggerStateBuilder.build("Lat Pulldown", "Back", sets, SampleData.ACTIVE_SESSION_ID, "kg", SampleData.now),
            RestUi(true, 74, 120), "kg", 20.0, LoggerActions()
        )
    }

    @Test fun workoutSmallLargeText() = compose.snap("R_small_workout", large) {
        ActiveWorkoutScreen(
            ActiveWorkoutStateBuilder.build(all, SampleData.ACTIVE_SESSION_ID, "kg", SampleData.now),
            32 * 60 + 8, RestUi(false, 0, 0), "kg", ActiveWorkoutActions()
        )
    }

    @Test fun exerciseSmallLargeText() = compose.snap("R_small_exercise", large) {
        val sets = SampleData.allSets.filter { it.exercise == "Bench Press" }
        ExerciseDetailScreen(
            ExerciseDetailStateBuilder.build("Bench Press", sets, "kg", SampleData.now), "kg", SampleData.now,
            ExerciseDetailActions(), initialTab = ExerciseTab.STRENGTH, initialRange = TimeRange.M3
        )
    }

    @Test fun bodySmallLargeText() = compose.snap("R_small_body", large) {
        val state = BodyStateBuilder.build(
            SampleData.bodyWeights, SampleData.allSets, SampleData.sessions.map { it.session.startTime }, "kg", SampleData.now
        )
        BodyScreen(state, "kg", SampleData.now, BodyActions())
    }

    @Test fun programLogSmallLargeText() = compose.snap("R_small_program_log", large) {
        ProgramLogContent(
            listOf(SessionExerciseLog(1, 9, 1, "Incline Smith / DB Press", 1, 102.5, 10, 102.5, 9)),
            onBack = {}, onFinish = {}, onSave = {}
        )
    }

    @Test @Config(qualifiers = WIDE)
    fun homeWide() = compose.snap("R_wide_home") {
        HomeScreen(HomeStateBuilder.build(SampleData.sessions, null, emptyList(), "kg", SampleData.now), 0, HomeActions())
    }

    @Test @Config(qualifiers = WIDE)
    fun loggerWide() = compose.snap("R_wide_logger") {
        val sets = all.flatMap { it.sets }.filter { it.exercise == "Face Pull" }
        LoggerScreen(
            LoggerStateBuilder.build("Face Pull", "Back", sets, SampleData.ACTIVE_SESSION_ID, "kg", SampleData.now),
            RestUi(false, 0, 0), "kg", 20.0, LoggerActions()
        )
    }
}
