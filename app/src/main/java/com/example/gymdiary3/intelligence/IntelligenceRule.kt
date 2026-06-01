package com.example.gymdiary3.intelligence

import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.TrainingSnapshot

interface IntelligenceRule {
    fun evaluate(snapshot: TrainingSnapshot): List<FitnessInsight>
}
