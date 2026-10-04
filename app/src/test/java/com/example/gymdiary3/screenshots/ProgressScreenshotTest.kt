package com.example.gymdiary3.screenshots

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.domain.analytics.TimeRange
import com.example.gymdiary3.presentation.exercise.ExerciseDetailStateBuilder
import com.example.gymdiary3.presentation.exercise.ExerciseTab
import com.example.gymdiary3.presentation.progress.ProgressStateBuilder
import com.example.gymdiary3.screens.ExerciseDetailActions
import com.example.gymdiary3.screens.ExerciseDetailScreen
import com.example.gymdiary3.screens.ProgressActions
import com.example.gymdiary3.screens.ProgressContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_TALL, application = Application::class)
class ProgressScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun progress() = compose.snap("S07_progress") {
        val state = ProgressStateBuilder.build(SampleData.sessions, emptyList(), "kg", SampleData.now)
        ProgressContent(state, "kg", SampleData.now, ProgressActions())
    }

    private fun detail(name: String, tab: ExerciseTab, range: TimeRange, shot: String) = compose.snap(shot) {
        val sets = SampleData.allSets.filter { it.exercise == name }
        val state = ExerciseDetailStateBuilder.build(name, sets, "kg", SampleData.now)
        ExerciseDetailScreen(state, "kg", SampleData.now, ExerciseDetailActions(), initialTab = tab, initialRange = range)
    }

    @Test fun exerciseStrength() = detail("Bench Press", ExerciseTab.STRENGTH, TimeRange.W8, "S08_exercise_strength")
    @Test fun exerciseVolume() = detail("Bench Press", ExerciseTab.VOLUME, TimeRange.M3, "S08_exercise_volume")
    @Test fun exerciseReps() = detail("Hammer Curl", ExerciseTab.REPS, TimeRange.ALL, "S08_exercise_reps")
}
