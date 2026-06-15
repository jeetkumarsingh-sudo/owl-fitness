package com.example.gymdiary3.core.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.gymdiary3.core.database.entity.BodyWeightEntity

@Dao
interface BodyWeightDao {
    @Insert
    suspend fun insertWeight(weight: BodyWeightEntity)

    @Update
    suspend fun updateWeight(bodyWeight: BodyWeightEntity)

    @Delete
    suspend fun deleteWeight(bodyWeight: BodyWeightEntity)

    @Query("SELECT * FROM BodyWeight ORDER BY timestamp DESC")
    fun getWeights(): Flow<List<BodyWeightEntity>>

    @Query("SELECT * FROM BodyWeight ORDER BY timestamp DESC")
    suspend fun getAllBodyWeightsList(): List<BodyWeightEntity>

    @Query("SELECT weight FROM BodyWeight ORDER BY timestamp DESC LIMIT 1")
    fun getLatestBodyWeightFlow(): Flow<Double?>

    @Query("SELECT * FROM BodyWeight WHERE timestamp >= :start AND timestamp < :end")
    suspend fun getWeightBetween(start: Long, end: Long): List<BodyWeightEntity>
}
