package com.example.gymdiary3.flows

import android.app.Application
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.core.database.WorkoutDatabase
import com.example.gymdiary3.data.repository.BodyWeightRepositoryImpl
import com.example.gymdiary3.data.repository.ExerciseRepositoryImpl
import com.example.gymdiary3.data.repository.WorkoutRepositoryImpl
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.model.EquipmentType
import com.example.gymdiary3.domain.model.Exercise
import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.system.backup.BackupManager
import com.example.gymdiary3.system.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

/**
 * Flows F01 (log a workout) and F06 (back up / restore) against a real Room
 * database in memory, through the same repositories and managers the app uses.
 * Case IDs match replica/test-plan.md.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], application = Application::class)
class DataFlowsTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var db: WorkoutDatabase
    private lateinit var workouts: WorkoutRepositoryImpl
    private lateinit var weights: BodyWeightRepositoryImpl
    private lateinit var exercises: ExerciseRepositoryImpl

    private val hour = 60 * 60 * 1000L

    @Before fun setUp() {
        db = newDb()
        workouts = WorkoutRepositoryImpl(db.workoutDao())
        weights = BodyWeightRepositoryImpl(db.bodyWeightDao())
        exercises = ExerciseRepositoryImpl(db.workoutDao())
    }

    @After fun tearDown() = db.close()

    private fun newDb() = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java).allowMainThreadQueries().build()

    private fun set(sessionId: Int, exercise: String, n: Int, kg: Double, reps: Int, at: Long, notes: String? = null) =
        WorkoutSet(0, at, "Back", exercise, n, reps, kg, false, sessionId, null, notes)

    // ---------------------------------------------------------------- F01

    @Test fun `F01-H1 start, log and finish keeps the session with an end time`() = runBlocking {
        val sm = SessionManager(workouts)
        sm.startSession()
        val id = sm.currentSessionId.value!!
        workouts.insertSet(set(id, "Lat Pulldown", 1, 50.0, 8, System.currentTimeMillis()))
        var finished: Int? = null
        sm.endSession { finished = it }
        assertEquals(id, finished)
        assertNotNull(workouts.getSessionById(id)?.endTime)
        assertNull(sm.currentSessionId.value)
    }

    @Test fun `F01-E1 finishing a session with no sets deletes it`() = runBlocking {
        val sm = SessionManager(workouts)
        sm.startSession()
        val id = sm.currentSessionId.value!!
        var finished: Int? = null
        sm.endSession { finished = it }
        assertEquals(-1, finished)
        assertNull(workouts.getSessionById(id))
    }

    @Test fun `F01-E2 a stale session is closed if it has sets and deleted if empty`() = runBlocking {
        val start = System.currentTimeMillis() - 13 * hour
        val withSets = workouts.insertSession(WorkoutSession(0, start, null, null, null)).toInt()
        workouts.insertSet(set(withSets, "Squat", 1, 80.0, 5, start + 60_000))
        SessionManager(workouts).initialize()
        assertEquals(start + hour, workouts.getSessionById(withSets)?.endTime)

        val empty = workouts.insertSession(WorkoutSession(0, System.currentTimeMillis() - 14 * hour, null, null, null)).toInt()
        SessionManager(workouts).initialize()
        assertNull(workouts.getSessionById(empty))
    }

    @Test fun `F01-E3 starting twice keeps one session`() = runBlocking {
        val sm = SessionManager(workouts)
        sm.startSession(); sm.startSession()
        assertEquals(1, workouts.getSessionsWithSets().first().size)
    }

    // ---------------------------------------------------------------- F06

    private suspend fun seedHistory() {
        val t = 1_780_000_000_000L
        val s1 = workouts.insertSession(WorkoutSession(0, t, t + hour, "Pull", "felt strong")).toInt()
        workouts.insertSet(set(s1, "Lat Pulldown", 1, 50.0, 8, t + 60_000))
        workouts.insertSet(set(s1, "Lat Pulldown", 2, 50.0, 7, t + 240_000, notes = "grip slipped, \"wide\""))
        val s2 = workouts.insertSession(WorkoutSession(0, t + 48 * hour, t + 49 * hour, "Legs", null)).toInt()
        workouts.insertSet(set(s2, "Développé couché 💪", 1, 60.0, 6, t + 48 * hour + 60_000))
        weights.insertWeight(BodyWeight(0, t, 72.4))
        weights.insertWeight(BodyWeight(0, t + 24 * hour, 72.1))
        exercises.insertExercise(Exercise(name = "Développé couché 💪", primaryMuscleGroup = "Chest", isCustom = true))
    }

    /**
     * The JSON Back up writes. (exportJson wraps the same text in a FileProvider
     * uri; Robolectric on Windows cannot resolve that provider root, so the
     * share step is checked by hand on a device — test plan F06-M1.)
     */
    private suspend fun backupFile(): File =
        File(context.cacheDir, "gym_diary_backup_test.json").apply {
            writeText(BackupManager(workouts, weights, exercises).backupJson())
        }

    @Test fun `F06-H1 a backup restores every session, set, weight and custom exercise into an empty install`() = runBlocking {
        seedHistory()
        val file = backupFile()

        val fresh = newDb()
        try {
            val w2 = WorkoutRepositoryImpl(fresh.workoutDao())
            val b2 = BodyWeightRepositoryImpl(fresh.bodyWeightDao())
            val e2 = ExerciseRepositoryImpl(fresh.workoutDao())
            val result = BackupManager(w2, b2, e2).importJson(context, Uri.fromFile(file))
            assertTrue(result.exceptionOrNull()?.toString() ?: "", result.isSuccess)

            val restored = w2.getSessionsWithSets().first().sortedBy { it.session.startTime }
            assertEquals(listOf("Pull", "Legs"), restored.map { it.session.name })
            assertEquals("felt strong", restored[0].session.notes)
            assertEquals(listOf(8, 7), restored[0].sets.sortedBy { it.setNumber }.map { it.reps })
            assertEquals("grip slipped, \"wide\"", restored[0].sets.first { it.setNumber == 2 }.notes)
            assertEquals("Développé couché 💪", restored[1].sets.single().exercise)
            assertEquals(listOf(72.4, 72.1), b2.getAllWeights().sortedBy { it.timestamp }.map { it.weight })
            assertTrue(e2.getAllExercises().any { it.name == "Développé couché 💪" && it.isCustom })
        } finally {
            fresh.close()
        }
    }

    @Test fun `F06-E1 restoring the same backup twice adds nothing the second time`() = runBlocking {
        seedHistory()
        val file = backupFile()
        val manager = BackupManager(workouts, weights, exercises)
        manager.importJson(context, Uri.fromFile(file))
        manager.importJson(context, Uri.fromFile(file))
        assertEquals(2, workouts.getSessionsWithSets().first().size)
        assertEquals(3, workouts.getSessionsWithSets().first().sumOf { it.sets.size })
        assertEquals(2, weights.getAllWeights().size)
    }

    @Test fun `F06-E2 a backup shared without its json extension still restores`() = runBlocking {
        seedHistory()
        val renamed = File(context.cacheDir, "Document from a friend").apply { writeText(backupFile().readText()) }
        val fresh = newDb()
        try {
            val w2 = WorkoutRepositoryImpl(fresh.workoutDao())
            val result = BackupManager(w2, BodyWeightRepositoryImpl(fresh.bodyWeightDao()), ExerciseRepositoryImpl(fresh.workoutDao()))
                .importJson(context, Uri.fromFile(renamed))
            assertTrue(result.isSuccess)
            assertEquals(2, w2.getSessionsWithSets().first().size)
        } finally {
            fresh.close()
        }
    }

    @Test fun `F06-N1 a corrupt file fails and writes nothing`() = runBlocking {
        seedHistory()
        val broken = File(context.cacheDir, "broken.json").apply { writeText(backupFile().readText().dropLast(40)) }
        val fresh = newDb()
        try {
            val w2 = WorkoutRepositoryImpl(fresh.workoutDao())
            val e2 = ExerciseRepositoryImpl(fresh.workoutDao())
            val result = BackupManager(w2, BodyWeightRepositoryImpl(fresh.bodyWeightDao()), e2).importJson(context, Uri.fromFile(broken))
            assertTrue(result.isFailure)
            assertEquals(0, w2.getSessionsWithSets().first().size)
            assertTrue(e2.getAllExercises().none { it.isCustom })
        } finally {
            fresh.close()
        }
    }

    @Test fun `F06-N2 a backup from a newer version with an unknown equipment type restores fully`() = runBlocking {
        seedHistory()
        // A newer app adds an equipment type this build does not know. One unknown
        // value must not abort the restore halfway (exercises in, sessions not).
        val text = backupFile().readText().replaceFirst("\"equipment\": \"${EquipmentType.OTHER.name}\"", "\"equipment\": \"SANDBAG\"")
        assertTrue("fixture contains the edited field", text.contains("SANDBAG"))
        val newer = File(context.cacheDir, "newer.json").apply { writeText(text) }
        val fresh = newDb()
        try {
            val w2 = WorkoutRepositoryImpl(fresh.workoutDao())
            val e2 = ExerciseRepositoryImpl(fresh.workoutDao())
            val result = BackupManager(w2, BodyWeightRepositoryImpl(fresh.bodyWeightDao()), e2).importJson(context, Uri.fromFile(newer))
            assertTrue(result.exceptionOrNull()?.toString() ?: "", result.isSuccess)
            assertEquals(2, w2.getSessionsWithSets().first().size)
            assertEquals(EquipmentType.OTHER, e2.getAllExercises().first { it.name == "Développé couché 💪" }.equipment)
        } finally {
            fresh.close()
        }
    }
}
