package com.example.gymdiary3.domain

import com.example.gymdiary3.data.BodyWeight

data class BodyWeightStats(
    val latestWeight: Double,
    val firstWeight: Double,
    val minWeight: Double,
    val maxWeight: Double,
    val totalChange: Double,
    val averageWeight: Double
)

object BodyWeightAnalyzer {
    fun getStats(weights: List<BodyWeight>): BodyWeightStats? {
        if (weights.isEmpty()) return null
        
        val sorted = weights.sortedBy { it.date }
        val latest = sorted.last().weight
        val first = sorted.first().weight
        val min = weights.minOf { it.weight }
        val max = weights.maxOf { it.weight }
        val avg = weights.map { it.weight }.average()
        
        return BodyWeightStats(
            latestWeight = latest,
            firstWeight = first,
            minWeight = min,
            maxWeight = max,
            totalChange = latest - first,
            averageWeight = avg
        )
    }
}
