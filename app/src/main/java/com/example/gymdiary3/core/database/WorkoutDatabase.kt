package com.example.gymdiary3.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gymdiary3.core.database.dao.BodyWeightDao
import com.example.gymdiary3.core.database.dao.WorkoutDao
import com.example.gymdiary3.core.database.entity.BodyWeightEntity
import com.example.gymdiary3.core.database.entity.ExerciseEntity
import com.example.gymdiary3.core.database.entity.WorkoutSessionEntity
import com.example.gymdiary3.core.database.entity.WorkoutSetEntity
import com.example.gymdiary3.core.database.migration.MIGRATION_8_9
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WorkoutSetEntity::class,
        BodyWeightEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class
    ],
    version = 9,
    exportSchema = true
)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyWeightDao(): BodyWeightDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE WorkoutSet RENAME COLUMN date TO timestamp")
                database.execSQL("ALTER TABLE session ADD COLUMN name TEXT")
                database.execSQL("ALTER TABLE WorkoutSet RENAME COLUMN support TO isAssisted")
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
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
