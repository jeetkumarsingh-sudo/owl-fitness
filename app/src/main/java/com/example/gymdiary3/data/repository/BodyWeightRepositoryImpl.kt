package com.example.gymdiary3.data.repository

import com.example.gymdiary3.data.BodyWeight
import com.example.gymdiary3.database.BodyWeightDao
import com.example.gymdiary3.database.WorkoutDao
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BodyWeightRepositoryImpl @Inject constructor(
    private val bodyWeightDao: BodyWeightDao,
    private val workoutDao: WorkoutDao
) : BodyWeightRepository {
    override suspend fun insertWeight(weight: BodyWeight) = bodyWeightDao.insertWeight(weight)
    override suspend fun updateWeight(bodyWeight: BodyWeight) = bodyWeightDao.updateWeight(bodyWeight)
    override suspend fun deleteWeight(bodyWeight: BodyWeight) = bodyWeightDao.deleteWeight(bodyWeight)
    override fun getWeights(): Flow<List<BodyWeight>> = bodyWeightDao.getWeights()
    override suspend fun getWeightBetween(start: Long, end: Long): List<BodyWeight> = bodyWeightDao.getWeightBetween(start, end)
    override fun getLatestBodyWeightFlow(): Flow<Double?> = workoutDao.getLatestBodyWeightFlow()
    override suspend fun getAllBodyWeightsList(): List<BodyWeight> = workoutDao.getAllBodyWeightsList()
}
