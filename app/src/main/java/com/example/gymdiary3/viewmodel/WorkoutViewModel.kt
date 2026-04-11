package com.example.gymdiary3.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.data.Exercise
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.database.WorkoutDao
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.service.RecommendationEngine
import com.example.gymdiary3.presentation.state.ExerciseUiState
import com.example.gymdiary3.system.session.SessionManager
import com.example.gymdiary3.system.timer.RestTimerManager
import com.example.gymdiary3.system.export.ExportFormatter
import com.example.gymdiary3.domain.settings.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(
    private val workoutDao: WorkoutDao,
    val settingsRepository: UserSettingsRepository? = null
) : ViewModel() {

    // System Managers
    val sessionManager = SessionManager(workoutDao)
    val restTimerManager = RestTimerManager(viewModelScope)

    // Data Pipeline
    val workouts: StateFlow<List<WorkoutSet>> = workoutDao.getWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<SessionWithSets>> = workoutDao.getSessionsWithSets()
        .map { WorkoutAnalyzer.filterValidSessions(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionsWithSets = sessions
    val currentSessionId = sessionManager.currentSessionId
    val totalWorkoutCount: StateFlow<Int> = sessions.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedMuscle = MutableStateFlow("")
    val exercisesByMuscle: StateFlow<List<Exercise>> = _selectedMuscle
        .flatMapLatest { muscle ->
            if (muscle.isEmpty()) flowOf(emptyList())
            else workoutDao.getExercisesByMuscle(muscle)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastSet = MutableStateFlow<WorkoutSet?>(null)
    val lastSet: StateFlow<WorkoutSet?> = _lastSet.asStateFlow()

    // Task 8: Derived flow for suggested weight
    val suggestedWeight: StateFlow<Double?> = _lastSet
        .map { it?.let { WorkoutAnalyzer.getSuggestedWeight(it.weight) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentSet = MutableStateFlow(1)
    val currentSet: StateFlow<Int> = _currentSet.asStateFlow()

    val latestBodyWeight: StateFlow<Double?> = workoutDao.getLatestBodyWeightFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isRestTimerRunning: StateFlow<Boolean> = restTimerManager.isRestTimerRunning
    val restTimerSeconds: StateFlow<Int> = restTimerManager.restTimerSeconds

    fun skipRestTimer() {
        restTimerManager.skipTimer()
    }

    fun getLastThreeSets(exercise: String): Flow<List<WorkoutSet>> {
        return workoutDao.getLastThreeSets(exercise)
    }

    init {
        insertDefaultWorkouts()
        viewModelScope.launch {
            workoutDao.deleteEmptySessions()
        }
    }

    fun loadLastSet(exerciseName: String) {
        viewModelScope.launch {
            _lastSet.value = workoutDao.getLastSet(exerciseName)
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

    fun startSession() {
        viewModelScope.launch {
            sessionManager.startSession()
        }
    }

    fun endSession(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            sessionManager.endSession(onComplete)
        }
    }

    fun getLastWeekSetsForExercise(exerciseName: String): Flow<List<WorkoutSet>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val thisWeekStart = cal.timeInMillis
        val lastWeekStart = thisWeekStart - 7L * 24 * 60 * 60 * 1000
        return workoutDao.getSetsForExerciseInDateRange(exerciseName, lastWeekStart, thisWeekStart)
    }

    suspend fun getHistoricBest1RM(exerciseName: String, excludeSessionId: Long): Double {
        return workoutDao.getHistoricBest1RM(exerciseName, excludeSessionId) ?: 0.0
    }

    fun startRestTimer(seconds: Int) {
        restTimerManager.startTimer(seconds)
    }

    // Task 3 & 4: Delegate to Analyzer and RecommendationEngine
    fun getExerciseUiState(exercise: String): ExerciseUiState {
        val stats = WorkoutAnalyzer.getExerciseStats(exercise, workouts.value.filter { it.exercise == exercise })
        return ExerciseUiState(
            exercise = stats.exercise,
            trend = stats.trend,
            trendLabel = WorkoutAnalyzer.getTrendLabel(stats.trend),
            isPR = stats.isPR,
            recommendation = RecommendationEngine.getRecommendation(stats),
            best1RM = stats.best1RM
        )
    }

    fun insertWorkout(muscle: String, exercise: String, setNumber: Int, reps: Int, weight: Double, support: Boolean) {
        val sessionId = sessionManager.currentSessionId.value ?: return 
        if (!WorkoutAnalyzer.isValidSet(weight, reps)) return
        
        viewModelScope.launch {
            workoutDao.insertWorkout(WorkoutSet(0, System.currentTimeMillis(), muscle, exercise, setNumber, reps, weight, support, sessionId))
            updateSetNumber(exercise)
            
            val defaultRest = settingsRepository?.userSettingsFlow?.firstOrNull()?.defaultRestSeconds ?: 90
            restTimerManager.startTimer(defaultRest)
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

    suspend fun exportAllDataToCsv(context: Context): Uri? = withContext(Dispatchers.IO) {
        if (sessions.value.isEmpty()) return@withContext null
        val bodyWeights = workoutDao.getAllBodyWeightsList()
        val csvContent = ExportFormatter.buildCsv(sessions.value, bodyWeights)
        return@withContext com.example.gymdiary3.data.FileHandler.writeToCache(context, csvContent)
    }

    private fun insertDefaultWorkouts() {
        viewModelScope.launch {
            val existing = workoutDao.getAllExercisesList()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    Exercise("Bench Press", "Chest"), Exercise("Incline Bench Press", "Chest"),
                    Exercise("Deadlift", "Back"), Exercise("Pullups", "Back"),
                    Exercise("Squat", "Legs"), Exercise("Leg Press", "Legs"),
                    Exercise("Overhead Press", "Shoulders"), Exercise("Lateral Raise", "Shoulders"),
                    Exercise("Barbell Curl", "Biceps"), Exercise("Hammer Curl", "Biceps"),
                    Exercise("Triceps Pushdown", "Triceps"), Exercise("Dips", "Triceps"),
                    Exercise("Plank", "Abs"), Exercise("Crunches", "Abs")
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

    fun getRecentExercises(): List<Pair<String, String>> {
        return sessions.value
            .sortedByDescending { it.session.startTime }
            .take(10)
            .flatMap { it.sets }
            .map { it.exercise to it.muscle }
            .distinctBy { it.first }
    }

    fun getMuscleGroups(): List<String> {
        return sessions.value
            .flatMap { it.sets }
            .map { it.muscle }
            .distinct()
            .sorted()
    }

    fun getExercisesByMuscle(muscle: String): List<String> {
        return sessions.value
            .flatMap { it.sets }
            .filter { it.muscle == muscle }
            .map { it.exercise }
            .distinct()
            .sorted()
    }
}
