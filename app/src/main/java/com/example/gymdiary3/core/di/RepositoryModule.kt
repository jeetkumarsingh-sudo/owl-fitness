package com.example.gymdiary3.core.di

import com.example.gymdiary3.data.repository.BodyWeightRepositoryImpl
import com.example.gymdiary3.data.repository.ExerciseRepositoryImpl
import com.example.gymdiary3.data.repository.SettingsRepositoryImpl
import com.example.gymdiary3.data.repository.WorkoutRepositoryImpl
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import com.example.gymdiary3.domain.repository.ExerciseRepository
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindBodyWeightRepository(impl: BodyWeightRepositoryImpl): BodyWeightRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
