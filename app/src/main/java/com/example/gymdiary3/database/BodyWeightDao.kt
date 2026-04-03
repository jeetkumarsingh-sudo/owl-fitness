package com.example.gymdiary3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.gymdiary3.data.BodyWeight

@Dao
interface BodyWeightDao {

    @Insert
    suspend fun insertWeight(weight: BodyWeight)

    @Query("SELECT * FROM BodyWeight ORDER BY date DESC")
    fun getWeights(): Flow<List<BodyWeight>>
}