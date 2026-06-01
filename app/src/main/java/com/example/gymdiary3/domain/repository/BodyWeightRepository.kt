package com.example.gymdiary3.domain.repository

import com.example.gymdiary3.domain.model.BodyWeight
import kotlinx.coroutines.flow.Flow

interface BodyWeightRepository {
    suspend fun insertWeight(weight: BodyWeight)
    suspend fun updateWeight(bodyWeight: BodyWeight)
    suspend fun deleteWeight(bodyWeight: BodyWeight)
    fun getWeights(): Flow<List<BodyWeight>>
    suspend fun getWeightBetween(start: Long, end: Long): List<BodyWeight>
    fun getLatestBodyWeightFlow(): Flow<Double?>
    suspend fun getAllWeights(): List<BodyWeight>
}
