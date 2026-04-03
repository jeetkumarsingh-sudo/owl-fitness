package com.example.gymdiary3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gymdiary3.data.BodyWeight
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.data.WorkoutSession

@Database(
    entities = [WorkoutSet::class, BodyWeight::class, Exercise::class, WorkoutSession::class],
    version = 6,
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
