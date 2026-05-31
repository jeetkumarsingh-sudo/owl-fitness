package com.example.gymdiary3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymdiary3.data.BodyWeight
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.data.WorkoutSession

@Database(
    entities = [WorkoutSet::class, BodyWeight::class, Exercise::class, WorkoutSession::class],
    version = 8,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyWeightDao(): BodyWeightDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Rename 'date' column to 'timestamp' in WorkoutSet
                database.execSQL("ALTER TABLE WorkoutSet RENAME COLUMN date TO timestamp")
                // Add 'name' column to session table (nullable, no default needed)
                database.execSQL("ALTER TABLE session ADD COLUMN name TEXT")
                // Add 'isAssisted' column, renaming from 'support'
                database.execSQL("ALTER TABLE WorkoutSet RENAME COLUMN support TO isAssisted")
                // Rename 'date' column to 'timestamp' in BodyWeight
                database.execSQL("ALTER TABLE BodyWeight RENAME COLUMN date TO timestamp")
            }
        }

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "gym_database"
                )
                    .addMigrations(MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
