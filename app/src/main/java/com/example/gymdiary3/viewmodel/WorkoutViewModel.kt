package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.WorkoutSession
import com.example.gymdiary3.database.WorkoutDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    val workouts: StateFlow<List<WorkoutSet>> = workoutDao.getWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dayTypes: StateFlow<List<String>> = workoutDao.getAllDayTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDayType = MutableStateFlow("")
    val exercisesByDay: StateFlow<List<Exercise>> = _selectedDayType
        .flatMapLatest { dayType ->
            if (dayType.isEmpty()) flowOf(emptyList())
            else workoutDao.getExercisesByDayType(dayType)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMuscle = MutableStateFlow("")
    val exercisesByMuscle: StateFlow<List<Exercise>> = _selectedMuscle
        .flatMapLatest { muscle ->
            if (muscle.isEmpty()) flowOf(emptyList())
            else workoutDao.getExercisesByMuscle(muscle)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastSet = MutableStateFlow<WorkoutSet?>(null)
    val lastSet: StateFlow<WorkoutSet?> = _lastSet

    private val _suggestedWeight = MutableStateFlow<Double?>(null)
    val suggestedWeight: StateFlow<Double?> = _suggestedWeight

    private val _currentSet = MutableStateFlow(1)
    val currentSet: StateFlow<Int> = _currentSet

    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId

    init {
        insertDefaultWorkouts()
    }

    fun loadLastSet(exerciseName: String) {
        viewModelScope.launch {
            val last = workoutDao.getLastSet(exerciseName)
            _lastSet.value = last
            _suggestedWeight.value = last?.let { getSuggestedWeight(it.weight) }
        }
    }

    private fun getSuggestedWeight(lastWeight: Double): Double {
        return when {
            lastWeight < 20 -> lastWeight + 2.5
            lastWeight < 50 -> lastWeight + 2.5
            else -> lastWeight + 5
        }
    }

    fun updateSetNumber(exerciseName: String) {
        viewModelScope.launch {
            val count = workoutDao.getTodaySetCount(exerciseName)
            _currentSet.value = count + 1
        }
    }

    fun selectDayType(dayType: String) {
        _selectedDayType.value = dayType
    }

    fun selectMuscle(muscle: String) {
        _selectedMuscle.value = muscle
    }

    fun startSession() {
        if (_currentSessionId.value != null) return // Already active
        viewModelScope.launch {
            val session = WorkoutSession(
                startTime = System.currentTimeMillis(),
                endTime = null
            )
            val id = workoutDao.insertSession(session).toInt()
            _currentSessionId.value = id
        }
    }

    fun endSession() {
        viewModelScope.launch {
            val id = _currentSessionId.value ?: return@launch
            val session = WorkoutSession(
                id = id,
                startTime = 0, // Placeholder, usually you'd fetch the original session or just update the endTime
                endTime = System.currentTimeMillis()
            )
            workoutDao.updateSession(session)
            _currentSessionId.value = null
        }
    }

    fun insertDefaultWorkouts() {
        viewModelScope.launch {
            val existing = workoutDao.getAllExercisesList()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    // DAY 1 – PUSH
                    Exercise("Pec Deck", "Chest", "Day 1 - Push"),
                    Exercise("Smith Incline Press", "Chest", "Day 1 - Push"),
                    Exercise("Smith Flat Bench", "Chest", "Day 1 - Push"),
                    Exercise("Bench Press", "Chest", "Day 1 - Push"),
                    Exercise("Fly", "Chest", "Day 1 - Push"),
                    Exercise("Triceps Pushdown", "Triceps", "Day 1 - Push"),
                    Exercise("Push-ups", "Chest", "Day 1 - Push"),

                    // DAY 2 – PULL
                    Exercise("Pull-ups", "Back", "Day 2 - Pull"),
                    Exercise("Deadlift", "Back", "Day 2 - Pull"),
                    Exercise("Lat Pulldown", "Back", "Day 2 - Pull"),
                    Exercise("Seated Row", "Back", "Day 2 - Pull"),
                    Exercise("Barbell Curl", "Biceps", "Day 2 - Pull"),
                    Exercise("Dumbbell Curl", "Biceps", "Day 2 - Pull"),

                    // DAY 3 – LEGS
                    Exercise("Squat", "Legs", "Day 3 - Legs"),
                    Exercise("Leg Press", "Legs", "Day 3 - Legs"),
                    Exercise("Leg Extension", "Legs", "Day 3 - Legs"),
                    Exercise("Leg Curl / RDL", "Legs", "Day 3 - Legs"),
                    Exercise("Calf Raises", "Legs", "Day 3 - Legs"),
                    Exercise("Abs", "Abs", "Day 3 - Legs"),

                    // DAY 5 – UPPER
                    Exercise("Bench Press ", "Chest", "Day 5 - Upper"),
                    Exercise("Lat Pulldown ", "Back", "Day 5 - Upper"),
                    Exercise("Overhead Press", "Shoulders", "Day 5 - Upper"),
                    Exercise("Lateral Raises", "Shoulders", "Day 5 - Upper"),
                    Exercise("Push-ups ", "Chest", "Day 5 - Upper"),
                    Exercise("Pull-ups ", "Back", "Day 5 - Upper"),

                    // DAY 6 – LOWER
                    Exercise("Squat (light)", "Legs", "Day 6 - Lower"),
                    Exercise("RDL (light)", "Legs", "Day 6 - Lower"),
                    Exercise("Abs ", "Abs", "Day 6 - Lower")
                )
                defaults.forEach { workoutDao.insertExercise(it) }
            }
        }
    }

    fun addExercise(name: String, muscle: String, dayType: String = "Custom") {
        viewModelScope.launch {
            workoutDao.insertExercise(Exercise(name, muscle, dayType, true))
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            workoutDao.deleteExercise(exercise)
        }
    }

    fun insertWorkout(muscle: String, exercise: String, set: Int, reps: Int, weight: Double, support: Boolean) {
        val sessionId = _currentSessionId.value ?: return // Safety: Do not allow insert if session is null
        viewModelScope.launch {
            workoutDao.insertWorkout(WorkoutSet(0, System.currentTimeMillis(), muscle, exercise, set, reps, weight, support, sessionId))
            // Ensure set number updates AFTER insertion to avoid race condition
            val count = workoutDao.getTodaySetCount(exercise)
            _currentSet.value = count + 1
        }
    }
}
