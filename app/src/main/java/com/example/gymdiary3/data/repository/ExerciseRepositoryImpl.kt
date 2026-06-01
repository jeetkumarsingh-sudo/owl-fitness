package com.example.gymdiary3.data.repository

import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.database.WorkoutDao
import com.example.gymdiary3.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : ExerciseRepository {
    override suspend fun insertExercise(exercise: Exercise) = workoutDao.insertExercise(exercise)
    override fun getExercisesByMuscle(muscle: String): Flow<List<Exercise>> = workoutDao.getExercisesByMuscle(muscle)
    override suspend fun deleteExercise(exercise: Exercise) = workoutDao.deleteExercise(exercise)
    override suspend fun getAllExercisesList(): List<Exercise> = workoutDao.getAllExercisesList()
}
