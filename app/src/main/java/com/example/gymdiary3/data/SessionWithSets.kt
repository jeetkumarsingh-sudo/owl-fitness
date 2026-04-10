package com.example.gymdiary3.data

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymdiary3.domain.util.WorkoutCalculations

@Immutable
data class SessionWithSets(
    @Embedded val session: WorkoutSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSet>
) {
    val date: Long get() = session.startTime
    
    val exercises: Map<String, List<WorkoutSet>> 
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
