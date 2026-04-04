package com.example.gymdiary3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.example.gymdiary3.data.BodyWeight

@Dao
interface BodyWeightDao {

    @Insert
    suspend fun insertWeight(weight: BodyWeight)

    @Update
    suspend fun updateWeight(bodyWeight: BodyWeight)

    @Delete
    suspend fun deleteWeight(bodyWeight: BodyWeight)

    @Query("SELECT * FROM BodyWeight ORDER BY date DESC")
    fun getWeights(): Flow<List<BodyWeight>>

    @Query("SELECT * FROM BodyWeight WHERE date >= :start AND date < :end")
    suspend fun getWeightBetween(start: Long, end: Long): List<BodyWeight>
}