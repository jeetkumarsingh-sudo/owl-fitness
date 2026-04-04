package com.example.gymdiary3.data

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation

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
        get() = calculateVolume(sets)

    val best1RM: Double
        get() = sets.maxOfOrNull { calculate1RM(it.weight, it.reps) } ?: 0.0
        
    val duration: Long
        get() = (session.endTime ?: session.startTime) - session.startTime

    val volumePerMuscle: Map<String, Double>
        get() = sets.groupBy { it.muscle }
            .mapValues { (_, muscleSets) -> calculateVolume(muscleSets) }
}

fun calculateVolume(sets: List<WorkoutSet>): Double {
    return sets.sumOf { it.weight * it.reps }
}

fun calculate1RM(weight: Double, reps: Int): Double {
    if (weight <= 0.0) return 0.0
    return weight * (1 + reps / 30.0)
}
