package com.example.gymdiary3.domain.history

import com.example.gymdiary3.domain.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionSplitTest {

    private fun sets(vararg muscles: Pair<String, Int>) = muscles.flatMap { (m, n) ->
        (1..n).map { WorkoutSet(timestamp = 0L, muscle = m, exercise = m, setNumber = it, reps = 8, weight = 20.0, isAssisted = false) }
    }

    @Test fun push() = assertEquals("Push", SessionSplit.label(sets("Chest" to 6, "Shoulders" to 2, "Triceps" to 3)))
    @Test fun pull() = assertEquals("Pull", SessionSplit.label(sets("Back" to 13, "Biceps" to 6)))
    @Test fun legs() = assertEquals("Lower body", SessionSplit.label(sets("Legs" to 12, "Abs" to 3)))
    @Test fun arms() = assertEquals("Arms", SessionSplit.label(sets("Biceps" to 4, "Triceps" to 4)))
    @Test fun upper() = assertEquals("Upper body", SessionSplit.label(sets("Chest" to 4, "Back" to 4)))
    @Test fun fullBody() = assertEquals("Full body", SessionSplit.label(sets("Back" to 3, "Legs" to 6, "Shoulders" to 6)))
    @Test fun coreOnly() = assertEquals("Core", SessionSplit.label(sets("Abs" to 4)))
    @Test fun empty() = assertEquals("Workout", SessionSplit.label(emptyList()))
}
