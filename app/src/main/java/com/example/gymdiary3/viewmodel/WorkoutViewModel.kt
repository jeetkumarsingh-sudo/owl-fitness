package com.example.gymdiary3.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.WorkoutSession
import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.database.WorkoutDao
import com.example.gymdiary3.utils.WorkoutCalculations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

    private var currentStartTime: Long = 0L

    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()

    private var restTimerJob: kotlinx.coroutines.Job? = null

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

    fun startRestTimer(seconds: Int = 90) {
        restTimerJob?.cancel()
        _restTimerSeconds.value = seconds
        _isRestTimerRunning.value = true
        restTimerJob = viewModelScope.launch {
            while (_restTimerSeconds.value > 0) {
                delay(1000L)
                _restTimerSeconds.value -= 1
            }
            _isRestTimerRunning.value = false
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _restTimerSeconds.value = 0
        _isRestTimerRunning.value = false
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
            val totalVolume = sessionWithSets?.totalVolume ?: 0.0

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
                    Exercise("Straight Arm Pulldown", "Back"),
                    
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

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            workoutDao.deleteExercise(exercise)
        }
    }

    fun validateSet(weight: Double, reps: Int): Boolean {
        if (weight < 0) return false
        if (reps <= 0) return false
        return true
    }

    fun insertWorkout(muscle: String, exercise: String, setNumber: Int, reps: Int, weight: Double, support: Boolean) {
        val sessionId = _currentSessionId.value ?: return 
        if (!validateSet(weight, reps)) return
        
        val volume = WorkoutCalculations.calculateVolume(weight, reps)
        if (volume <= 0) return

        viewModelScope.launch {
            workoutDao.insertWorkout(WorkoutSet(0, System.currentTimeMillis(), muscle, exercise, setNumber, reps, weight, support, sessionId))
            val count = workoutDao.getTodaySetCount(exercise)
            _currentSet.value = count + 1
            startRestTimer()
        }
    }

    suspend fun exportAllDataToCsv(context: Context): Uri? = withContext(Dispatchers.IO) {
        try {
            val sessionsVal = sessions.value
            if (sessionsVal.isEmpty()) return@withContext null

            val sb = StringBuilder()

            // Section 1: Workout Sessions
            sb.appendLine("=== WORKOUT SESSIONS ===")
            sb.appendLine("Session ID,Date,Duration (min),Total Sets,Total Volume (kg)")

            val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

            for (sessionWithSets in sessionsVal.sortedByDescending { it.session.startTime }) {
                val session = sessionWithSets.session
                val sets = sessionWithSets.sets
                val totalVolume = sessionWithSets.totalVolume
                val durationMin = sessionWithSets.duration / 60_000
                sb.appendLine(
                    "${session.id}," +
                    "\"${dateFormat.format(Date(session.startTime))}\"," +
                    "$durationMin," +
                    "${sets.size}," +
                    "%.1f".format(totalVolume)
                )
            }

            sb.appendLine()

            // Section 2: All Sets
            sb.appendLine("=== ALL SETS ===")
            sb.appendLine("Date,Session ID,Exercise,Muscle Group,Set Number,Weight (kg),Reps,Volume (kg),Est. 1RM (kg)")

            for (sessionWithSets in sessionsVal.sortedByDescending { it.session.startTime }) {
                val dateStr = dateFormat.format(Date(sessionWithSets.session.startTime))
                for (set in sessionWithSets.sets.sortedBy { it.setNumber }) {
                    val volume = WorkoutCalculations.calculateVolume(set.weight, set.reps)
                    val est1rmVal = WorkoutCalculations.calculate1RM(set.weight, set.reps)
                    val est1rm = if (est1rmVal > 0) "%.1f".format(est1rmVal) else "N/A"
                    sb.appendLine(
                        "\"$dateStr\"," +
                        "${set.sessionId}," +
                        "\"${set.exercise}\"," +
                        "\"${set.muscle}\"," +
                        "${set.setNumber}," +
                        "${set.weight}," +
                        "${set.reps}," +
                        "%.1f".format(volume) + "," +
                        est1rm
                    )
                }
            }

            sb.appendLine()

            // Section 3: Body Weight History
            sb.appendLine("=== BODY WEIGHT HISTORY ===")
            sb.appendLine("Date,Weight (kg)")
            
            val bodyWeights = workoutDao.getAllBodyWeightsList()
            for (bw in bodyWeights.sortedByDescending { it.date }) {
                sb.appendLine("\"${dateFormat.format(Date(bw.date))}\",${bw.weight}")
            }

            // Write to cache file
            val fileName = "owl_fitness_export_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(sb.toString())

            // Return FileProvider URI
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
