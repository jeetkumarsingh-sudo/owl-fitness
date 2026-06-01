package com.example.gymdiary3.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymdiary3.core.database.entity.WorkoutSessionEntity
import com.example.gymdiary3.core.database.entity.WorkoutSetEntity
import com.example.gymdiary3.domain.util.WorkoutCalculations

data class SessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSetEntity>
) {
    val date: Long get() = session.startTime
    
    val exercises: Map<String, List<WorkoutSetEntity>> 
        get() = sets.groupBy { it.exercise }
        
    val totalVolume: Double 
        get() = sets.sumOf { WorkoutCalculations.calculateVolume(it.weight, it.reps) }

    val duration: Long
        get() = (session.endTime ?: session.startTime) - session.startTime

    val volumePerMuscle: Map<String, Double>
        get() = sets.groupBy { it.muscle }
            .mapValues { (_, muscleSets) -> 
                muscleSets.sumOf { WorkoutCalculations.calculateVolume(it.weight, it.reps) } 
            }
}
