package com.example.gymdiary3.flows

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.domain.model.ProgramDay
import com.example.gymdiary3.domain.model.SessionExerciseLog
import com.example.gymdiary3.domain.model.SessionSchedule
import com.example.gymdiary3.domain.settings.UserSettings
import com.example.gymdiary3.presentation.body.BodyStateBuilder
import com.example.gymdiary3.presentation.history.HistoryStateBuilder
import com.example.gymdiary3.presentation.history.SessionEntry
import com.example.gymdiary3.presentation.workout.LoggerStateBuilder
import com.example.gymdiary3.presentation.workout.PickerStateBuilder
import com.example.gymdiary3.domain.model.Exercise
import com.example.gymdiary3.screens.*
import com.example.gymdiary3.screenshots.PHONE
import com.example.gymdiary3.screenshots.SampleData
import com.example.gymdiary3.ui.theme.GymDiaryTheme
import com.example.gymdiary3.viewmodel.RestUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The screens driven the way a person would — taps, long-presses and typing —
 * asserting what each action hands to the app. Case IDs match replica/test-plan.md.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE, application = Application::class)
class FlowUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val noRest = RestUi(false, 0, 0)

    private fun loggerFor(exercise: String) = LoggerStateBuilder.build(
        exercise, "Back", SampleData.sessionsWithActive.flatMap { it.sets }.filter { it.exercise == exercise },
        SampleData.ACTIVE_SESSION_ID, "kg", SampleData.now
    )

    // ------------------------------------------------------------ F01 logger

    @Test fun `F01-H2 logging the pre-filled set hands over exactly what is shown`() {
        val logged = mutableListOf<Pair<Double, Int>>()
        val state = loggerFor("Lat Pulldown")
        compose.setContent {
            GymDiaryTheme { LoggerScreen(state, noRest, "kg", 20.0, LoggerActions(onLog = { kg, reps, _, _, _ -> logged += kg to reps })) }
        }
        compose.onNodeWithText("Log set ${state.nextSetNumber}").performClick()
        assertEquals(listOf(50.0 to 8), logged)
    }

    @Test fun `F01-H3 steppers change the set before it is logged`() {
        val logged = mutableListOf<Pair<Double, Int>>()
        val state = loggerFor("Lat Pulldown")
        compose.setContent {
            GymDiaryTheme { LoggerScreen(state, noRest, "kg", 20.0, LoggerActions(onLog = { kg, reps, _, _, _ -> logged += kg to reps })) }
        }
        compose.onNodeWithContentDescription("Increase Weight (kg)").performClick()
        compose.onNodeWithContentDescription("Decrease Reps").performClick()
        compose.onNodeWithText("Log set ${state.nextSetNumber}").performClick()
        assertEquals(listOf(52.5 to 7), logged)
    }

    @Test fun `F01-E7 after a PR today the logger stops calling the lift stalled`() {
        // Sample: Lat Pulldown stalled at 47.5 kg for 6 sessions, then 50 x 9 today (a PR).
        val state = loggerFor("Lat Pulldown")
        compose.setContent { GymDiaryTheme { LoggerScreen(state, noRest, "kg", 20.0, LoggerActions()) } }
        compose.onNodeWithText("PR TODAY").assertExists()
        assertEquals(0, compose.onAllNodesWithText("STALLING").fetchSemanticsNodes().size)
        assertEquals(0, compose.onAllNodesWithText("Same weight for 6 sessions").fetchSemanticsNodes().size)
    }

    @Test fun `F01-E5 a double tap on Log records one set, not two copies of set 4`() {
        val logged = mutableListOf<Pair<Double, Int>>()
        val state = loggerFor("Lat Pulldown")
        compose.setContent {
            GymDiaryTheme { LoggerScreen(state, noRest, "kg", 20.0, LoggerActions(onLog = { kg, reps, _, _, _ -> logged += kg to reps })) }
        }
        // The second tap lands before the saved set comes back and advances the screen to set 5.
        compose.onNodeWithText("Log set ${state.nextSetNumber}").performClick().performClick()
        assertEquals(1, logged.size)
    }

    @Test fun `F01-E6 an adjusted weight survives rotation`() {
        val restore = StateRestorationTester(compose)
        val state = loggerFor("Lat Pulldown")
        restore.setContent { GymDiaryTheme { LoggerScreen(state, noRest, "kg", 20.0, LoggerActions()) } }
        compose.onNodeWithContentDescription("Increase Weight (kg)").performClick()
        restore.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("52.5").assertExists()
    }

    @Test fun `F08-H1 long-pressing a logged set asks before deleting it`() {
        val deleted = mutableListOf<Int>()
        val state = loggerFor("Lat Pulldown")
        compose.setContent {
            GymDiaryTheme { LoggerScreen(state, noRest, "kg", 20.0, LoggerActions(onDeleteSet = { deleted += it })) }
        }
        compose.onNodeWithText("50 × 9").performTouchInput { longClick() }
        assertTrue("nothing deleted before confirming", deleted.isEmpty())
        compose.onNodeWithText("Delete").performClick()
        assertEquals(1, deleted.size)
    }

    // ------------------------------------------------------------ F01 picker

    @Test fun `F01-E4 search finds accented and emoji exercise names`() {
        val library = listOf(Exercise(name = "Développé couché 💪", primaryMuscleGroup = "Chest", isCustom = true))
        val state = PickerStateBuilder.build(library, emptyList(), "dév", null, "kg", SampleData.now)
        compose.setContent { GymDiaryTheme { ExercisePickerScreen(state, "dév", null, PickerActions()) } }
        compose.onNodeWithText("Développé couché 💪").assertExists()
    }

    // ------------------------------------------------------------ F03 / F08 history

    @Test fun `F08-H2 long-pressing a session asks, then deletes that session`() {
        val deleted = mutableListOf<Int>()
        val state = HistoryStateBuilder.build(SampleData.sessions, null, "kg")
        val first = state.months.first().entries.filterIsInstance<SessionEntry>().first()
        compose.setContent { GymDiaryTheme { HistoryScreen(state, HistoryActions(onDelete = { deleted += it })) } }
        compose.onAllNodesWithText(first.title)[0].performTouchInput { longClick() }
        compose.onNodeWithText("Delete").performClick()
        assertEquals(listOf(first.sessionId), deleted)
    }

    // ------------------------------------------------------------ F04 body weight

    @Test fun `F04-H1 typing a weight with a comma logs it`() {
        val logged = mutableListOf<Double>()
        val state = BodyStateBuilder.build(emptyList(), emptyList(), emptyList(), "kg", SampleData.now)
        compose.setContent { GymDiaryTheme { BodyScreen(state, "kg", SampleData.now, BodyActions(onLog = { logged += it })) } }
        compose.onNodeWithText("Log").assertIsNotEnabled()
        compose.onNodeWithText("Today's weight").performTextInput("72,4")
        compose.onNodeWithText("Log").assertIsEnabled().performClick()
        assertEquals(listOf(72.4), logged)
    }

    @Test fun `F04-N1 letters are ignored and a bare dot cannot be logged`() {
        val state = BodyStateBuilder.build(emptyList(), emptyList(), emptyList(), "kg", SampleData.now)
        compose.setContent { GymDiaryTheme { BodyScreen(state, "kg", SampleData.now, BodyActions()) } }
        compose.onNodeWithText("Today's weight").performTextInput("abc.")
        compose.onNodeWithText("Log").assertIsNotEnabled()
    }

    // ------------------------------------------------------------ F06 settings

    @Test fun `F06-H2 settings controls report the chosen values`() {
        val picked = mutableListOf<String>()
        compose.setContent {
            GymDiaryTheme {
                SettingsContent(
                    UserSettings(),
                    SettingsActions(
                        onUnit = { picked += "unit:$it" }, onRest = { picked += "rest:$it" }, onBar = { picked += "bar:$it" },
                        onExportCsv = { picked += "csv" }, onBackup = { picked += "backup" }, onRestore = { picked += "restore" }
                    )
                )
            }
        }
        compose.onNodeWithText("lbs").performClick()
        compose.onNodeWithText("2 min").performClick()
        compose.onNodeWithText("15 kg").performClick()
        compose.onNodeWithText("Export CSV").performClick()
        compose.onNodeWithText("Back up").performClick()
        compose.onNodeWithText("Restore from backup").performClick()
        assertEquals(listOf("unit:lbs", "rest:120", "bar:15.0", "csv", "backup", "restore"), picked)
    }

    // ------------------------------------------------------------ F05 programs

    private val days = listOf(
        ProgramDay(1, "Day 1 — PUSH", 1, "Push", 70, "Chest"),
        ProgramDay(4, "Day 4 — REST", 4, "Rest", 20, "Walk"),
    )

    @Test fun `F05-H1 a program day can be planned for today and then started`() {
        val events = mutableListOf<String>()
        val planned = SessionSchedule(9, "Day 1 — PUSH", SampleData.now, 1, "Planned")
        compose.setContent {
            GymDiaryTheme {
                ProgramsContent(days, listOf(planned), SampleData.now, ProgramActions(
                    onPlanToday = { events += "plan:${it.id}" }, onStart = { events += "start:${it.id}" }
                ))
            }
        }
        // Push is already planned today, so it shows "Planned", not a second "Plan today".
        compose.onNodeWithText("Planned").assertExists()
        assertEquals(0, compose.onAllNodesWithText("Plan today").fetchSemanticsNodes().size)
        compose.onNodeWithText("Start").performClick()
        assertEquals(listOf("start:9"), events)
    }

    @Test fun `F05-E1 rest days cannot be planned`() {
        compose.setContent { GymDiaryTheme { ProgramsContent(days, emptyList(), SampleData.now, ProgramActions()) } }
        assertEquals(1, compose.onAllNodesWithText("Plan today").fetchSemanticsNodes().size)
    }

    @Test fun `F05-H2 program log saves typed sets, accepting a comma decimal`() {
        val saved = mutableListOf<SessionExerciseLog>()
        val log = SessionExerciseLog(3, 9, 3, "RDL", 3)
        compose.setContent { GymDiaryTheme { ProgramLogContent(listOf(log), onBack = {}, onFinish = {}, onSave = { saved += it }) } }
        compose.onNodeWithContentDescription("RDL set 1 weight").performTextInput("62,5")
        compose.onNodeWithContentDescription("RDL set 1 reps").performTextInput("8")
        compose.onNodeWithText("Save").performClick()
        assertEquals(log.copy(set1Weight = 62.5, set1Reps = 8), saved.single())
    }
}
