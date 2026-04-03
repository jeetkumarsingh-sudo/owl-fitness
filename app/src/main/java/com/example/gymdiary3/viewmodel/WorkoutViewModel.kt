package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.database.WorkoutDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    // Single source of truth for workouts
    val workouts: StateFlow<List<WorkoutSet>> = workoutDao.getWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedMuscle = MutableStateFlow("")
    
    // Reactive exercise list based on selected muscle
    val exercises: StateFlow<List<Exercise>> = _selectedMuscle
        .flatMapLatest { muscle ->
            if (muscle.isEmpty()) flowOf(emptyList())
            else workoutDao.getExercisesByMuscle(muscle)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectMuscle(muscle: String) {
        _selectedMuscle.value = muscle
    }

    fun addExercise(name: String, muscle: String) {
        viewModelScope.launch {
            workoutDao.insertExercise(Exercise(name, muscle, true))
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            workoutDao.deleteExercise(exercise)
        }
    }

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

    fun seedExercises() {
        viewModelScope.launch {
            val defaults = listOf(
                Exercise("Deadlift", "Back"), Exercise("Bench Press", "Chest"),
                Exercise("Squat", "Legs"), Exercise("Overhead Press", "Shoulders")
            )
            defaults.forEach { workoutDao.insertExercise(it) }
        }
    }
}
