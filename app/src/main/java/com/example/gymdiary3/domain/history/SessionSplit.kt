package com.example.gymdiary3.domain.history

import com.example.gymdiary3.domain.model.WorkoutSet

/**
 * Names a session by what was trained, from the share of sets per movement
 * family. One family with 70%+ of the sets names the session; otherwise it is
 * upper body (almost no legs) or full body. Abs never decide the label.
 */
object SessionSplit {

    private val push = setOf("Chest", "Shoulders", "Triceps")
    private val pull = setOf("Back", "Biceps")
    private val legs = setOf("Legs")

    fun label(sets: List<WorkoutSet>): String {
        val counted = sets.filter { it.muscle != "Abs" }
        if (counted.isEmpty()) return if (sets.isNotEmpty()) "Core" else "Workout"

        val total = counted.size.toDouble()
        val pushShare = counted.count { it.muscle in push } / total
        val pullShare = counted.count { it.muscle in pull } / total
        val legShare = counted.count { it.muscle in legs } / total

        val arms = counted.all { it.muscle == "Biceps" || it.muscle == "Triceps" } &&
            counted.any { it.muscle == "Biceps" } && counted.any { it.muscle == "Triceps" }

        return when {
            arms -> "Arms"
            pushShare >= 0.7 -> "Push"
            pullShare >= 0.7 -> "Pull"
            legShare >= 0.7 -> "Lower body"
            legShare < 0.15 -> "Upper body"
            else -> "Full body"
        }
    }
}
