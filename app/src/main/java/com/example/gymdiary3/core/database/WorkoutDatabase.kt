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

@Database(
    entities = [
        WorkoutSetEntity::class,
        BodyWeightEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
    ],
    version = 8,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyWeightDao(): BodyWeightDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "gym_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
