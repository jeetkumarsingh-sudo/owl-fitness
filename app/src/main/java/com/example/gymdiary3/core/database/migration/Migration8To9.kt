package com.example.gymdiary3.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // --- WorkoutSet: add rpe (nullable float) and notes (nullable text) ---
        database.execSQL("ALTER TABLE WorkoutSet ADD COLUMN rpe REAL")
        database.execSQL("ALTER TABLE WorkoutSet ADD COLUMN notes TEXT")

        // --- Exercise: expansion via recreate ---
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS Exercise_new (
                name TEXT NOT NULL PRIMARY KEY,
                primaryMuscleGroup TEXT NOT NULL DEFAULT '',
                secondaryMuscleGroups TEXT NOT NULL DEFAULT '',
                equipment TEXT NOT NULL DEFAULT 'OTHER',
                movementPattern TEXT NOT NULL DEFAULT 'ISOLATION',
                trackingType TEXT NOT NULL DEFAULT 'WEIGHT_REPS',
                isCustom INTEGER NOT NULL DEFAULT 0,
                isArchived INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Copy data: muscle -> primaryMuscleGroup
        database.execSQL("""
            INSERT INTO Exercise_new 
                (name, primaryMuscleGroup, secondaryMuscleGroups, equipment, movementPattern, trackingType, isCustom, isArchived)
            SELECT 
                name,
                muscle,
                '',
                CASE 
                    WHEN muscle IN ('Chest', 'Back', 'Legs', 'Shoulders') THEN 'BARBELL'
                    ELSE 'OTHER'
                END,
                'ISOLATION',
                'WEIGHT_REPS',
                isCustom,
                0
            FROM Exercise
        """.trimIndent())

        database.execSQL("DROP TABLE IF EXISTS Exercise")
        database.execSQL("ALTER TABLE Exercise_new RENAME TO Exercise")

        database.execSQL("CREATE INDEX IF NOT EXISTS idx_exercise_equipment ON Exercise(equipment)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_exercise_muscle ON Exercise(primaryMuscleGroup)")
    }
}
