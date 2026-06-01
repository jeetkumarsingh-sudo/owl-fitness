package com.example.gymdiary3.core.di

import android.content.Context
import com.example.gymdiary3.core.database.WorkoutDatabase
import com.example.gymdiary3.core.database.dao.WorkoutDao
import com.example.gymdiary3.core.database.dao.BodyWeightDao
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.system.session.SessionManager
import com.example.gymdiary3.system.timer.RestTimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WorkoutDatabase =
        WorkoutDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideWorkoutDao(db: WorkoutDatabase): WorkoutDao = db.workoutDao()

    @Provides
    @Singleton
    fun provideBodyWeightDao(db: WorkoutDatabase): BodyWeightDao = db.bodyWeightDao()

    @Provides
    @Singleton
    fun provideSessionManager(workoutRepository: WorkoutRepository): SessionManager =
        SessionManager(workoutRepository)

    @Provides
    @Singleton
    fun provideRestTimerManager(): RestTimerManager =
        RestTimerManager(CoroutineScope(SupervisorJob() + Dispatchers.Main))
}
