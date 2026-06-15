package com.example.gymdiary3.data.repository

import com.example.gymdiary3.core.database.dao.BodyWeightDao
import com.example.gymdiary3.data.mapper.toDomain
import com.example.gymdiary3.data.mapper.toEntity
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BodyWeightRepositoryImpl @Inject constructor(
    private val bodyWeightDao: BodyWeightDao
) : BodyWeightRepository {
    override suspend fun insertWeight(weight: BodyWeight) = bodyWeightDao.insertWeight(weight.toEntity())
    override suspend fun updateWeight(bodyWeight: BodyWeight) = bodyWeightDao.updateWeight(bodyWeight.toEntity())
    override suspend fun deleteWeight(bodyWeight: BodyWeight) = bodyWeightDao.deleteWeight(bodyWeight.toEntity())
    override fun getWeights(): Flow<List<BodyWeight>> = bodyWeightDao.getWeights().map { list -> list.map { it.toDomain() } }
    override suspend fun getWeightBetween(start: Long, end: Long): List<BodyWeight> = bodyWeightDao.getWeightBetween(start, end).map { it.toDomain() }
    override fun getLatestBodyWeightFlow(): Flow<Double?> = bodyWeightDao.getLatestBodyWeightFlow()
    override suspend fun getAllWeights(): List<BodyWeight> = bodyWeightDao.getAllBodyWeightsList().map { it.toDomain() }
}
