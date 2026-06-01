package com.example.gymdiary3.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gymdiary3.core.database.WorkoutDatabase
import com.example.gymdiary3.core.database.migration.MIGRATION_8_9
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WorkoutDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate7To8() {
        // Create database with version 7
        var db = helper.createDatabase(TEST_DB, 7)

        // Insert some data using SQL
        db.execSQL("INSERT INTO session (startTime) VALUES (1000)")
        db.execSQL("INSERT INTO WorkoutSet (date, muscle, exercise, setNumber, reps, weight, support) VALUES (1000, 'Chest', 'Bench Press', 1, 10, 60.0, 0)")
        db.execSQL("INSERT INTO BodyWeight (date, weight) VALUES (1000, 75.0)")
        db.execSQL("INSERT INTO Exercise (name, muscle, isCustom) VALUES ('Bench Press', 'Chest', 0)")
        db.close()

        // Run migration to version 8
        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, WorkoutDatabase.MIGRATION_7_8)

        // Verify data and new columns
        val sessionCursor = db.query("SELECT * FROM session")
        assert(sessionCursor.columnCount == 5) // id, startTime, endTime, name, notes
        sessionCursor.moveToFirst()
        assert(sessionCursor.getLong(sessionCursor.getColumnIndex("startTime")) == 1000L)
        sessionCursor.close()

        val workoutCursor = db.query("SELECT * FROM WorkoutSet")
        assert(workoutCursor.getColumnIndex("timestamp") != -1)
        assert(workoutCursor.getColumnIndex("isAssisted") != -1)
        workoutCursor.moveToFirst()
        assert(workoutCursor.getDouble(workoutCursor.getColumnIndex("weight")) == 60.0)
        workoutCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate8To9() {
        // Create database with version 8
        var db = helper.createDatabase(TEST_DB, 8)
        
        // Insert data representing version 8 schema
        // Note: version 8 already has 'notes' in session according to our audit
        db.execSQL("INSERT INTO session (startTime, notes) VALUES (2000, 'Session notes')")
        db.execSQL("INSERT INTO WorkoutSet (timestamp, muscle, exercise, setNumber, reps, weight, isAssisted) VALUES (2000, 'Back', 'Pullups', 1, 8, 0.0, 0)")
        db.execSQL("INSERT INTO Exercise (name, muscle, isCustom) VALUES ('Pullups', 'Back', 0)")
        db.close()

        // Run migration to version 9
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        // Verify WorkoutSet new columns
        val workoutCursor = db.query("SELECT * FROM WorkoutSet")
        assert(workoutCursor.getColumnIndex("rpe") != -1)
        assert(workoutCursor.getColumnIndex("notes") != -1)
        workoutCursor.close()

        // Verify Exercise table recreation and mapping
        val exerciseCursor = db.query("SELECT * FROM Exercise")
        assert(exerciseCursor.getColumnIndex("primaryMuscleGroup") != -1)
        assert(exerciseCursor.getColumnIndex("equipment") != -1)
        exerciseCursor.moveToFirst()
        assert(exerciseCursor.getString(exerciseCursor.getColumnIndex("name")) == "Pullups")
        assert(exerciseCursor.getString(exerciseCursor.getColumnIndex("primaryMuscleGroup")) == "Back")
        exerciseCursor.close()

        db.close()
    }

    @Test
    fun testFreshInstallV9() {
        // Create database with current version (9)
        val db = helper.createDatabase(TEST_DB, 9)
        db.close()
    }
}
