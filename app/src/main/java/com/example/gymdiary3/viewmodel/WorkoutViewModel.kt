package com.example.gymdiary3.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.model.Exercise
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.usecase.workout.*
import com.example.gymdiary3.presentation.state.ExerciseUiState
import com.example.gymdiary3.system.session.SessionManager
import com.example.gymdiary3.system.timer.RestTimerManager
import com.example.gymdiary3.system.export.ExportFormatter
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.domain.repository.ExerciseRepository
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val bodyWeightRepository: BodyWeightRepository,
    val settingsRepository: SettingsRepository,
    private val logSetUseCase: LogSetUseCase,
    private val startSessionUseCase: StartSessionUseCase,
    private val endSessionUseCase: EndSessionUseCase,
    private val getLastSessionSetsUseCase: GetLastSessionSetsUseCase,
    val sessionManager: SessionManager,
    val restTimerManager: RestTimerManager,
) : ViewModel() {

    // Data Pipeline
    val workouts: StateFlow<List<WorkoutSet>> = workoutRepository.getAllSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Exercises logged in current session  
    val exercisesThisSession: StateFlow<List<String>> = combine(
        sessionManager.currentSessionId,
        workouts
    ) { sessionId, allSets ->
        if (sessionId == null) emptyList()
        else allSets.asSequence()
             .filter { it.sessionId == sessionId }
             .map { it.exercise }
             .distinct()
             .toList()
    }
    .distinctUntilChanged()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Last set logged
    val lastSetLogged: StateFlow<WorkoutSet?> = workouts
        .map { sets -> sets.maxByOrNull { it.timestamp } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Time elapsed for current session
    val sessionDurationSeconds: StateFlow<Long> = sessionManager.currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId == null) flowOf(0L)
            else flow {
                val session = workoutRepository.getSessionById(sessionId)
                if (session != null) {
                    val startTime = session.startTime
                    while (true) {
                        val current = System.currentTimeMillis()
                        emit((current - startTime) / 1000)
                        // Precise delay to align with next second
                        val nextTick = 1000 - ((current - startTime) % 1000)
                        kotlinx.coroutines.delay(nextTick.milliseconds)
                    }
                } else {
                    emit(0L)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val sessions: StateFlow<List<SessionWithSets>> = workoutRepository.getSessionsWithSets()
        .map { WorkoutAnalyzer.filterValidSessions(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current session notes
    val currentSessionNotes: StateFlow<String?> = sessionManager.currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId == null) flowOf(null)
            else workoutRepository.getSessionFlowById(sessionId).map { it?.notes }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateSessionNotes(notes: String) {
        val sessionId = sessionManager.currentSessionId.value ?: return
        viewModelScope.launch {
            workoutRepository.getSessionById(sessionId)?.let { session ->
                workoutRepository.updateSession(session.copy(notes = notes))
            }
        }
    }

    val currentSessionId = sessionManager.currentSessionId
    val sessionsWithSets = sessions
    val totalWorkoutCount: StateFlow<Int> = sessions.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val exerciseUiStates: StateFlow<Map<String, ExerciseUiState>> = workouts
        .map { allSets ->
            allSets.groupBy { it.exercise }
                .mapValues { (exercise, sets) ->
                    val stats = WorkoutAnalyzer.getExerciseStats(exercise, sets)
                    val recommendation = when {
                        stats.isPR -> "New personal record! Consider increasing weight next session."
                        stats.trend > 0 -> "Progressing well. Maintain current overload strategy."
                        stats.trend < 0 -> "Slight regression. Check recovery and nutrition."
                        else -> "Weight stable. Try increasing reps or weight next session."
                    }
                    ExerciseUiState(
                        exercise = stats.exercise,
                        trend = stats.trend,
                        trendLabel = WorkoutAnalyzer.getTrendLabel(stats.trend),
                        isPR = stats.isPR,
                        recommendation = recommendation,
                        best1RM = stats.best1RM,
                        totalVolume = stats.totalVolume
                    )
                }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _selectedMuscle = MutableStateFlow("")
    val exercisesByMuscle: StateFlow<List<Exercise>> = _selectedMuscle
        .flatMapLatest { muscle ->
            if (muscle.isEmpty()) flowOf(emptyList())
            else exerciseRepository.getExercisesByMuscle(muscle)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastSet = MutableStateFlow<WorkoutSet?>(null)
    val lastSet: StateFlow<WorkoutSet?> = _lastSet.asStateFlow()

    // Task 8: Derived flow for suggested weight
    val suggestedWeight: StateFlow<Double?> = _lastSet
        .map { it?.let { WorkoutAnalyzer.getSuggestedWeight(it.weight) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentSet: StateFlow<Int> = combine(
        sessionManager.currentSessionId,
        workouts,
        _lastSet // Using this to know which exercise we are currently logging
    ) { sessionId, allSets, lastSet ->
        val exerciseName = lastSet?.exercise
        if ((sessionId == null) || (exerciseName == null)) 1
        else {
            allSets.count { it.sessionId == sessionId && it.exercise == exerciseName } + 1
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val isRestTimerRunning: StateFlow<Boolean> = restTimerManager.isRestTimerRunning
    val restTimerSeconds: StateFlow<Int> = restTimerManager.restTimerSeconds

    fun skipRestTimer() {
        restTimerManager.skipTimer()
    }

    init {
        viewModelScope.launch {
            sessionManager.initialize()
        }
    }

    fun loadLastSet(exerciseName: String) {
        viewModelScope.launch {
            _lastSet.value = workoutRepository.getLastSet(exerciseName)
        }
    }

    fun selectMuscle(muscle: String) {
        _selectedMuscle.value = muscle
    }

    fun startSession(sessionDateMillis: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            startSessionUseCase(sessionDateMillis)
        }
    }

    fun endSession(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            endSessionUseCase(onComplete)
        }
    }

    fun getLastSessionSetsForExercise(exerciseName: String, currentSessionId: Int): Flow<List<WorkoutSet>> {
        return getLastSessionSetsUseCase(exerciseName, currentSessionId)
    }

    suspend fun getHistoricBest1RM(exerciseName: String, excludeSessionId: Int): Double {
        return workoutRepository.getHistoricBest1RM(exerciseName, excludeSessionId) ?: 0.0
    }

    fun insertWorkout(
        muscle: String,
        exercise: String,
        setNumber: Int,
        reps: Int,
        weight: Double,
        isAssisted: Boolean,
        rpe: Float? = null,
        notes: String? = null
    ) {
        val sessionId = sessionManager.currentSessionId.value ?: return 
        if (!WorkoutAnalyzer.isValidSet(weight, reps)) return

        viewModelScope.launch {
            logSetUseCase(
                sessionId = sessionId,
                muscle = muscle,
                exercise = exercise,
                setNumber = setNumber,
                reps = reps,
                weight = weight,
                isAssisted = isAssisted,
                rpe = rpe,
                notes = notes
            )
            loadLastSet(exercise)
        }
    }

    fun deleteSession(id: Int) {
        viewModelScope.launch {
            if (sessionManager.currentSessionId.value == id) {
                sessionManager.clearSessionManually()
            }
            workoutRepository.deleteSessionById(id)
        }
    }

    fun deleteEmptySessions() {
        viewModelScope.launch {
            workoutRepository.deleteEmptySessions()
        }
    }

    suspend fun exportAllDataToCsv(context: Context): Uri? = withContext(Dispatchers.IO) {
        if (sessions.value.isEmpty()) return@withContext null
        val bodyWeights = bodyWeightRepository.getAllWeights()
        val unit = settingsRepository.userSettingsFlow.firstOrNull()?.weightUnit ?: "kg"
        val csvContent = ExportFormatter.buildCsv(sessions.value, bodyWeights, unit)
        return@withContext com.example.gymdiary3.data.FileHandler.writeToCache(context, csvContent)
    }

    fun addExercise(
        name: String, 
        muscle: String,
        equipment: com.example.gymdiary3.domain.model.EquipmentType = com.example.gymdiary3.domain.model.EquipmentType.OTHER,
        movementPattern: com.example.gymdiary3.domain.model.MovementPattern = com.example.gymdiary3.domain.model.MovementPattern.ISOLATION,
        trackingType: com.example.gymdiary3.domain.model.TrackingType = com.example.gymdiary3.domain.model.TrackingType.WEIGHT_REPS
    ) {
        viewModelScope.launch {
            exerciseRepository.insertExercise(
                Exercise(
                    name = name,
                    primaryMuscleGroup = muscle,
                    equipment = equipment,
                    movementPattern = movementPattern,
                    trackingType = trackingType,
                    isCustom = true
                )
            )
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exercise)
        }
    }
}
