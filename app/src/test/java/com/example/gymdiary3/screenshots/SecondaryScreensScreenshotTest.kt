package com.example.gymdiary3.screenshots

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.domain.model.ProgramDay
import com.example.gymdiary3.domain.model.SessionExerciseLog
import com.example.gymdiary3.domain.model.SessionSchedule
import com.example.gymdiary3.domain.settings.UserSettings
import com.example.gymdiary3.screens.ProgramActions
import com.example.gymdiary3.screens.ProgramLogContent
import com.example.gymdiary3.screens.ProgramsContent
import com.example.gymdiary3.screens.SettingsActions
import com.example.gymdiary3.screens.SettingsContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE, application = Application::class)
class SecondaryScreensScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val day = 24L * 60 * 60 * 1000

    private val programDays = listOf(
        ProgramDay(1, "Day 1 — PUSH", 1, "Push", 70, "Upper chest, side delts, overhead press"),
        ProgramDay(2, "Day 2 — PULL", 2, "Pull", 75, "Lats, face pulls, biceps"),
        ProgramDay(3, "Day 3 — LEGS", 3, "Legs", 70, "Squat, RDL, quad isolation"),
        ProgramDay(4, "Day 4 — REST", 4, "Rest", 20, "Recovery walk"),
        ProgramDay(5, "Day 5 — UPPER VOLUME", 5, "Upper Volume", 70, "Delts, arms, second lat session"),
        ProgramDay(6, "Day 6 — LIGHT LOWER", 6, "Light Lower", 50, "Technique and mobility"),
        ProgramDay(7, "Day 7 — REST", 7, "Rest", 0, "Full recovery"),
    )

    @Test
    fun settings() = compose.snap("S12_settings") {
        SettingsContent(UserSettings(), SettingsActions())
    }

    @Test
    fun programs() = compose.snap("S10_programs") {
        val now = SampleData.now // Sunday
        val sessions = listOf(
            SessionSchedule(1, "Day 1 — PUSH", now - 6 * day, 1, "Done"),
            SessionSchedule(2, "Day 2 — PULL", now - 5 * day, 2, "Done"),
            SessionSchedule(3, "Day 3 — LEGS", now - 3 * day, 3, "Done"),
            SessionSchedule(4, "Day 5 — UPPER VOLUME", now, 5, "Planned"),
        )
        ProgramsContent(programDays, sessions, now, ProgramActions())
    }

    @Test
    fun programLog() = compose.snap("S11_program_log") {
        ProgramLogContent(
            listOf(
                SessionExerciseLog(1, 9, 1, "Squat", 1, 80.0, 6, 80.0, 6, 80.0, 5, null, null, null, null, "Add 2.5 kg when 4×6 at RIR 2"),
                SessionExerciseLog(2, 9, 2, "Leg Press", 2, 140.0, 12, 140.0, 11),
                SessionExerciseLog(3, 9, 3, "RDL", 3),
                SessionExerciseLog(4, 9, 4, "Leg Extension", 4),
            ),
            onBack = {}, onFinish = {}, onSave = {}
        )
    }
}
