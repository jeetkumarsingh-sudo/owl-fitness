package com.example.gymdiary3.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymdiary3.core.database.entity.WorkoutSessionEntity
import com.example.gymdiary3.core.database.entity.WorkoutSetEntity
import com.example.gymdiary3.core.util.WorkoutCalculations

data class SessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSetEntity>
) {
    val totalVolume: Double 
        get() = sets.sumOf { it.weight * it.reps }
}
