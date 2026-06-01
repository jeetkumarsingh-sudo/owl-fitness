package com.example.gymdiary3.domain.repository

import com.example.gymdiary3.data.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    suspend fun insertExercise(exercise: Exercise)
    fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>>
    suspend fun deleteExercise(exercise: Exercise)
    suspend fun getAllExercisesList(): List<Exercise>
}
