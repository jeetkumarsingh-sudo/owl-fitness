package com.example.gymdiary3.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymdiary3.core.database.dao.BodyWeightDao
import com.example.gymdiary3.core.database.dao.ProgramDao
import com.example.gymdiary3.core.database.dao.WorkoutDao
import com.example.gymdiary3.core.database.entity.BodyWeightEntity
import com.example.gymdiary3.core.database.entity.ExerciseEntity
import com.example.gymdiary3.core.database.entity.ProgramDayEntity
import com.example.gymdiary3.core.database.entity.ProgramExerciseEntity
import com.example.gymdiary3.core.database.entity.SessionExerciseLogEntity
import com.example.gymdiary3.core.database.entity.SessionScheduleEntity
import com.example.gymdiary3.core.database.entity.WorkoutSessionEntity
import com.example.gymdiary3.core.database.entity.WorkoutSetEntity
import com.example.gymdiary3.core.database.migration.MIGRATION_8_9

@Database(
    entities = [
        WorkoutSetEntity::class,
        BodyWeightEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        ProgramDayEntity::class,
        ProgramExerciseEntity::class,
        SessionScheduleEntity::class,
        SessionExerciseLogEntity::class,
    ],
    version = 11,
    exportSchema = true,
)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun programDao(): ProgramDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Fix WorkoutSet indices to match Room's expected default names
                db.execSQL("DROP INDEX IF EXISTS idx_workout_exercise")
                db.execSQL("DROP INDEX IF EXISTS idx_workout_session")
                db.execSQL("DROP INDEX IF EXISTS idx_workout_timestamp")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkoutSet_exercise` ON `WorkoutSet` (`exercise`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkoutSet_sessionId` ON `WorkoutSet` (`sessionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkoutSet_timestamp` ON `WorkoutSet` (`timestamp`)")

                // Add missing index for session startTime
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_startTime` ON `session` (`startTime`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `program_days` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `dayNumber` INTEGER NOT NULL, 
                        `sessionType` TEXT NOT NULL, 
                        `plannedDuration` INTEGER, 
                        `primaryPriority` TEXT, 
                        `warmupNotes` TEXT
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `program_exercises` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `programDayId` INTEGER NOT NULL, 
                        `exerciseName` TEXT NOT NULL, 
                        `order` INTEGER NOT NULL, 
                        `setsPlanned` INTEGER NOT NULL, 
                        `repsPlanned` TEXT NOT NULL, 
                        `restSeconds` INTEGER NOT NULL, 
                        `notes` TEXT, 
                        `progressionRule` TEXT, 
                        `category` TEXT, 
                        FOREIGN KEY(`programDayId`) REFERENCES `program_days`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_exercises_programDayId` ON `program_exercises` (`programDayId`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `session_schedule` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `date` INTEGER NOT NULL, 
                        `programDayId` INTEGER, 
                        `status` TEXT NOT NULL, 
                        `notes` TEXT, 
                        FOREIGN KEY(`programDayId`) REFERENCES `program_days`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_schedule_programDayId` ON `session_schedule` (`programDayId`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `session_exercise_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `sessionId` INTEGER NOT NULL, 
                        `programExerciseId` INTEGER, 
                        `exerciseName` TEXT NOT NULL, 
                        `order` INTEGER NOT NULL, 
                        `set1Weight` REAL, `set1Reps` INTEGER, 
                        `set2Weight` REAL, `set2Reps` INTEGER, 
                        `set3Weight` REAL, `set3Reps` INTEGER, 
                        `set4Weight` REAL, `set4Reps` INTEGER, 
                        `set5Weight` REAL, `set5Reps` INTEGER, 
                        `notes` TEXT, 
                        FOREIGN KEY(`sessionId`) REFERENCES `session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`programExerciseId`) REFERENCES `program_exercises`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL 
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercise_logs_sessionId` ON `session_exercise_logs` (`sessionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercise_logs_programExerciseId` ON `session_exercise_logs` (`programExerciseId`)")
                
                // Fix: Also add the index that was added to WorkoutSessionEntity
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_startTime` ON `session` (`startTime`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // RENAME COLUMN not supported before SQLite 3.25.0 (Android 10)
                // Use recreate approach for compatibility with minSdk 26

                // WorkoutSet: drop + recreate with renamed columns
                db.execSQL("CREATE TABLE IF NOT EXISTS WorkoutSet_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "muscle TEXT NOT NULL, " +
                        "exercise TEXT NOT NULL, " +
                        "setNumber INTEGER NOT NULL, " +
                        "reps INTEGER NOT NULL, " +
                        "weight REAL NOT NULL, " +
                        "isAssisted INTEGER NOT NULL DEFAULT 0, " +
                        "sessionId INTEGER)")
                db.execSQL("INSERT INTO WorkoutSet_new (id, timestamp, muscle, exercise, setNumber, reps, weight, isAssisted, sessionId) " +
                        "SELECT id, date, muscle, exercise, setNumber, reps, weight, support, sessionId FROM WorkoutSet")
                db.execSQL("DROP TABLE IF EXISTS WorkoutSet")
                db.execSQL("ALTER TABLE WorkoutSet_new RENAME TO WorkoutSet")

                // session: add new columns
                db.execSQL("ALTER TABLE session ADD COLUMN name TEXT")
                db.execSQL("ALTER TABLE session ADD COLUMN notes TEXT")

                // BodyWeight: drop + recreate with renamed column
                db.execSQL("CREATE TABLE IF NOT EXISTS BodyWeight_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "weight REAL NOT NULL)")
                db.execSQL("INSERT INTO BodyWeight_new (id, timestamp, weight) " +
                        "SELECT id, date, weight FROM BodyWeight")
                db.execSQL("DROP TABLE IF EXISTS BodyWeight")
                db.execSQL("ALTER TABLE BodyWeight_new RENAME TO BodyWeight")

                // Recreate indices with Room's expected default names
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkoutSet_exercise` ON `WorkoutSet` (`exercise`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkoutSet_sessionId` ON `WorkoutSet` (`sessionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkoutSet_timestamp` ON `WorkoutSet` (`timestamp`)")
            }
        }

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "gym_database"
                )
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
