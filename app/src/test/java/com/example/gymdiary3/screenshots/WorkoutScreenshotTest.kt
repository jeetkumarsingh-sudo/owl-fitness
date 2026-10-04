package com.example.gymdiary3.screenshots

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.domain.model.Exercise
import com.example.gymdiary3.presentation.workout.ActiveWorkoutStateBuilder
import com.example.gymdiary3.presentation.workout.LoggerStateBuilder
import com.example.gymdiary3.presentation.workout.PickerStateBuilder
import com.example.gymdiary3.screens.*
import com.example.gymdiary3.viewmodel.RestUi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE, application = Application::class)
class WorkoutScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val all = SampleData.sessionsWithActive
    private val noRest = RestUi(false, 0, 0)

    @Test
    fun activeWorkout() = compose.snap("S04_workout") {
        val state = ActiveWorkoutStateBuilder.build(all, SampleData.ACTIVE_SESSION_ID, "kg", SampleData.now)
        ActiveWorkoutScreen(state, 32 * 60 + 8, RestUi(true, 74, 120), "kg", ActiveWorkoutActions())
    }

    @Test
    fun workoutJustStarted() = compose.snap("S04_workout_empty") {
        val history = SampleData.sessions
        val state = ActiveWorkoutStateBuilder.build(history, activeId = 1000, unit = "kg", now = SampleData.now)
        ActiveWorkoutScreen(state, 45, noRest, "kg", ActiveWorkoutActions())
    }

    @Test
    fun loggerMidExercise() = compose.snap("S05_logger") {
        val sets = all.flatMap { it.sets }.filter { it.exercise == "Lat Pulldown" }
        val state = LoggerStateBuilder.build("Lat Pulldown", "Back", sets, SampleData.ACTIVE_SESSION_ID, "kg", SampleData.now)
        LoggerScreen(state, RestUi(true, 74, 120), "kg", 20.0, LoggerActions())
    }

    @Test
    fun loggerFreshWithTarget() = compose.snap("S05_logger_target") {
        val sets = all.flatMap { it.sets }.filter { it.exercise == "Face Pull" }
        val state = LoggerStateBuilder.build("Face Pull", "Back", sets, SampleData.ACTIVE_SESSION_ID, "kg", SampleData.now)
        LoggerScreen(state, noRest, "kg", 20.0, LoggerActions())
    }

    @Test
    fun loggerOptionsOpen() {
        val sets = all.flatMap { it.sets }.filter { it.exercise == "Seated Row" }
        compose.snap("S05_logger_options") {
            val state = LoggerStateBuilder.build("Seated Row", "Back", sets, SampleData.ACTIVE_SESSION_ID, "kg", SampleData.now)
            LoggerScreen(state, noRest, "kg", 20.0, LoggerActions())
        }
        compose.onNodeWithText("RPE, notes and plates").performClick()
        compose.waitForIdle()
        captureRoot(compose, "S05_logger_options")
    }

    @Test
    fun picker() = compose.snap("S03_picker") {
        val library = listOf("Bench Press" to "Chest", "Squat" to "Legs", "Cable Crossover" to "Chest", "Pull-up" to "Back")
            .map { (n, m) -> Exercise(name = n, primaryMuscleGroup = m) }
        val state = PickerStateBuilder.build(library, SampleData.allSets, "", null, "kg", SampleData.now)
        ExercisePickerScreen(state, "", null, PickerActions())
    }
}
