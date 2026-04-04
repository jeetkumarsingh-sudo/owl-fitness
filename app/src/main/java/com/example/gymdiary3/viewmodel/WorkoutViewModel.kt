package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.WorkoutSession
import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.database.WorkoutDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SessionSummary(
    val totalSets: Int,
    val totalVolume: Double,
    val exercises: Map<String, List<WorkoutSet>>,
    val duration: Long,
    val bodyWeight: Double? = null,
    val date: Long
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    val workouts: StateFlow<List<WorkoutSet>> = workoutDao.getWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<SessionWithSets>> = workoutDao.getSessionsWithSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionsWithSets: StateFlow<List<SessionWithSets>> = sessions

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

    private val _summary = MutableStateFlow<SessionSummary?>(null)
    val summary: StateFlow<SessionSummary?> = _summary

    private var currentStartTime: Long = 0L

    private val _timer = MutableStateFlow(0)
    val timer: StateFlow<Int> = _timer

    init {
        insertDefaultWorkouts()
        viewModelScope.launch {
            workoutDao.deleteEmptySessions()
        }
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
            lastWeight < 20 -> lastWeight + 1.25
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

    fun selectMuscle(muscle: String) {
        _selectedMuscle.value = muscle
    }

    fun loadSummary(sessionId: Int) {
        viewModelScope.launch {
            val sessionWithSets = workoutDao.getSessionWithSetsById(sessionId) ?: return@launch
            
            val latestBodyWeight = workoutDao.getLatestBodyWeight()?.weight
            
            _summary.value = SessionSummary(
                totalSets = sessionWithSets.sets.size,
                totalVolume = sessionWithSets.totalVolume,
                exercises = sessionWithSets.exercises,
                duration = sessionWithSets.duration,
                bodyWeight = latestBodyWeight,
                date = sessionWithSets.date
            )
        }
    }

    private var timerJob: kotlinx.coroutines.Job? = null

    fun startRestTimer(seconds: Int = 90) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in seconds downTo 0) {
                _timer.value = i
                delay(1000)
            }
        }
    }

    fun startSession() {
        if (_currentSessionId.value != null) return // Already active
        viewModelScope.launch {
            currentStartTime = System.currentTimeMillis()
            val session = WorkoutSession(
                startTime = currentStartTime,
                endTime = null
            )
            val id = workoutDao.insertSession(session).toInt()
            _currentSessionId.value = id
        }
    }

    fun endSession(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val id = _currentSessionId.value ?: return@launch
            
            val sessionWithSets = workoutDao.getSessionWithSetsById(id)
            val sets = sessionWithSets?.sets ?: emptyList()
            val totalVolume = com.example.gymdiary3.data.calculateVolume(sets)

            if (totalVolume <= 0) {
                val session = sessionWithSets?.session ?: workoutDao.getSessionById(id)
                workoutDao.deleteSession(session)
                _currentSessionId.value = null
                currentStartTime = 0L
                return@launch
            }

            val session = WorkoutSession(
                id = id,
                startTime = currentStartTime,
                endTime = System.currentTimeMillis()
            )
            workoutDao.updateSession(session)
            _currentSessionId.value = null
            currentStartTime = 0L
            onComplete(id)
        }
    }

    fun deleteSession(id: Int) {
        viewModelScope.launch {
            workoutDao.deleteSessionById(id)
        }
    }

    fun deleteEmptySessions() {
        viewModelScope.launch {
            workoutDao.deleteEmptySessions()
        }
    }

    fun insertDefaultWorkouts() {
        viewModelScope.launch {
            val existing = workoutDao.getAllExercisesList()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    Exercise("Bench Press", "Chest"),
                    Exercise("Incline Bench Press", "Chest"),
                    Exercise("Dumbbell Fly", "Chest"),
                    Exercise("Push-ups", "Chest"),
                    
                    Exercise("Deadlift", "Back"),
                    Exercise("Pull-ups", "Back"),
                    Exercise("Lat Pulldown", "Back"),
                    Exercise("Seated Row", "Back"),
                    Exercise("One Arm Row", "Back"),
                    
                    Exercise("Squat", "Legs"),
                    Exercise("Leg Press", "Legs"),
                    Exercise("Leg Extension", "Legs"),
                    Exercise("Leg Curl", "Legs"),
                    Exercise("Calf Raise", "Legs"),
                    
                    Exercise("Overhead Press", "Shoulders"),
                    Exercise("Lateral Raise", "Shoulders"),
                    Exercise("Front Raise", "Shoulders"),
                    
                    Exercise("Barbell Curl", "Biceps"),
                    Exercise("Dumbbell Curl", "Biceps"),
                    Exercise("Hammer Curl", "Biceps"),
                    
                    Exercise("Triceps Pushdown", "Triceps"),
                    Exercise("Skull Crusher", "Triceps"),
                    Exercise("Dips", "Triceps"),
                    
                    Exercise("Plank", "Abs"),
                    Exercise("Crunch", "Abs"),
                    Exercise("Leg Raise", "Abs")
                )
                defaults.forEach { workoutDao.insertExercise(it) }
            }
        }
    }

    fun addExercise(name: String, muscle: String) {
        viewModelScope.launch {
            workoutDao.insertExercise(Exercise(name, muscle, true))
        }
    }

    fun calculateVolume(sets: List<WorkoutSet>): Double {
        return sets.sumOf { it.weight * it.reps }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            workoutDao.deleteExercise(exercise)
        }
    }

    fun insertWorkout(muscle: String, exercise: String, setNumber: Int, reps: Int, weight: Double, support: Boolean) {
        val sessionId = _currentSessionId.value ?: return 
        viewModelScope.launch {
            workoutDao.insertWorkout(WorkoutSet(0, System.currentTimeMillis(), muscle, exercise, setNumber, reps, weight, support, sessionId))
            val count = workoutDao.getTodaySetCount(exercise)
            _currentSet.value = count + 1
            startRestTimer()
        }
    }
}
