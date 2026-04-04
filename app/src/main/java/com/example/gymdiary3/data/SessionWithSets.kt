package com.example.gymdiary3.data

import androidx.room.Embedded
import androidx.room.Relation

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
        
    val duration: Long
        get() = (session.endTime ?: session.startTime) - session.startTime
}

fun calculateVolume(sets: List<WorkoutSet>): Double {
    return sets.sumOf { it.weight * it.reps }
}
