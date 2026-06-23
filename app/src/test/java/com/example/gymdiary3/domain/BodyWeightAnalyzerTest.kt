package com.example.gymdiary3.domain

import com.example.gymdiary3.domain.model.BodyWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BodyWeightAnalyzerTest {

    @Test
    fun `getStats returns null for empty list`() {
        assertNull(BodyWeightAnalyzer.getStats(emptyList()))
    }

    @Test
    fun `getStats calculates correct values`() {
        val weights = listOf(
            BodyWeight(1, 1000L, 75.0),
            BodyWeight(2, 2000L, 76.0),
            BodyWeight(3, 3000L, 74.0),
            BodyWeight(4, 4000L, 77.0)
        )
        val stats = BodyWeightAnalyzer.getStats(weights)!!
        assertEquals(77.0, stats.latestWeight, 0.01)
        assertEquals(75.0, stats.firstWeight, 0.01)
        assertEquals(74.0, stats.minWeight, 0.01)
        assertEquals(77.0, stats.maxWeight, 0.01)
        assertEquals(2.0, stats.totalChange, 0.01)
        assertEquals(75.5, stats.averageWeight, 0.01)
    }

    @Test
    fun `getStats handles single weight`() {
        val weights = listOf(BodyWeight(1, 1000L, 75.0))
        val stats = BodyWeightAnalyzer.getStats(weights)!!
        assertEquals(75.0, stats.latestWeight, 0.01)
        assertEquals(75.0, stats.averageWeight, 0.01)
        assertEquals(0.0, stats.totalChange, 0.01)
    }

    @Test
    fun `getStats returns latest weight correctly`() {
        val weights = listOf(
            BodyWeight(1, 3000L, 77.0),
            BodyWeight(2, 1000L, 75.0),
            BodyWeight(3, 2000L, 76.0)
        )
        val stats = BodyWeightAnalyzer.getStats(weights)!!
        assertEquals(77.0, stats.latestWeight, 0.01)
        assertEquals(75.0, stats.firstWeight, 0.01)
    }
}
