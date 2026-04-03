package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.database.WorkoutDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    val allWorkouts: StateFlow<List<WorkoutSet>> = workoutDao.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertWorkout(
        muscle: String,
        exercise: String,
        set: Int,
        reps: Int,
        weight: Double,
        support: Boolean
    ) {
        viewModelScope.launch {
            val workout = WorkoutSet(
                date = System.currentTimeMillis(),
                muscle = muscle,
                exercise = exercise,
                set = set,
                reps = reps,
                weight = weight,
                support = support
            )
            workoutDao.insertWorkout(workout)
        }
    }
}
