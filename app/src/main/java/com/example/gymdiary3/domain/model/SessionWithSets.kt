package com.example.gymdiary3.domain.model

import com.example.gymdiary3.core.util.WorkoutCalculations

data class SessionWithSets(
    val session: WorkoutSession,
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
