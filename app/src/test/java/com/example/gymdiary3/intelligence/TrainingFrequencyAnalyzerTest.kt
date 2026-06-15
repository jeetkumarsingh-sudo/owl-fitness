package com.example.gymdiary3.intelligence

import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.intelligence.model.TrainingSnapshot
import com.example.gymdiary3.intelligence.rules.TrainingFrequencyAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingFrequencyAnalyzerTest {

    private val analyzer = TrainingFrequencyAnalyzer()

    @Test
    fun `detects low frequency when sessions per week is low`() {
        val snapshot = TrainingSnapshot(
            allSessions = emptyList(),
            recentSessions = emptyList(),
            setsByExercise = emptyMap(),
            sessionsByWeek = emptyMap(),
            totalSetsAllTime = 0,
            trainingDaysLast28 = 2,
            averageSessionsPerWeek = 0.5,
            now = System.currentTimeMillis()
        )
        val insights = analyzer.evaluate(snapshot)
        assertEquals(1, insights.size)
        assertEquals(InsightType.TRAINING_FREQUENCY_LOW, insights.first().type)
    }

    @Test
    fun `detects optimal frequency when in ideal range`() {
        val snapshot = TrainingSnapshot(
            allSessions = emptyList(),
            recentSessions = emptyList(),
            setsByExercise = emptyMap(),
            sessionsByWeek = emptyMap(),
            totalSetsAllTime = 0,
            trainingDaysLast28 = 15,
            averageSessionsPerWeek = 3.75,
            now = System.currentTimeMillis()
        )
        val insights = analyzer.evaluate(snapshot)
        assertEquals(1, insights.size)
        assertEquals(InsightType.TRAINING_FREQUENCY_OPTIMAL, insights.first().type)
    }

    @Test
    fun `returns no insights when frequency in moderate range`() {
        val snapshot = TrainingSnapshot(
            allSessions = emptyList(),
            recentSessions = emptyList(),
            setsByExercise = emptyMap(),
            sessionsByWeek = emptyMap(),
            totalSetsAllTime = 0,
            trainingDaysLast28 = 10,
            averageSessionsPerWeek = 2.0,
            now = System.currentTimeMillis()
        )
        val insights = analyzer.evaluate(snapshot)
        assertTrue(insights.isEmpty())
    }

    @Test
    fun `no insight when no data`() {
        val snapshot = TrainingSnapshot(
            allSessions = emptyList(),
            recentSessions = emptyList(),
            setsByExercise = emptyMap(),
            sessionsByWeek = emptyMap(),
            totalSetsAllTime = 0,
            trainingDaysLast28 = 0,
            averageSessionsPerWeek = 0.0,
            now = System.currentTimeMillis()
        )
        val insights = analyzer.evaluate(snapshot)
        assertTrue(insights.isEmpty())
    }
}
