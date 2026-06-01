package com.example.gymdiary3.data.repository

import com.example.gymdiary3.core.database.dao.WorkoutDao
import com.example.gymdiary3.data.mapper.toDomain
import com.example.gymdiary3.data.mapper.toEntity
import com.example.gymdiary3.domain.model.Exercise
import com.example.gymdiary3.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : ExerciseRepository {
    override suspend fun insertExercise(exercise: Exercise) = workoutDao.insertExercise(exercise.toEntity())
    override fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>> = 
        workoutDao.getExercisesByMuscle(muscle).map { list -> list.map { it.toDomain() } }
    override suspend fun deleteExercise(exercise: Exercise) = workoutDao.deleteExercise(exercise.toEntity())
    override suspend fun getAllExercises(): List<Exercise> =
        workoutDao.getAllExercisesList().map { it.toDomain() }
}
