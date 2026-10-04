package com.example.gymdiary3.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.domain.model.EquipmentType
import com.example.gymdiary3.domain.model.Exercise
import com.example.gymdiary3.domain.model.MovementPattern
import com.example.gymdiary3.domain.model.TrackingType
import com.example.gymdiary3.domain.repository.ExerciseRepository
import com.example.gymdiary3.domain.repository.SettingsRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.domain.settings.UserSettings
import com.example.gymdiary3.domain.usecase.workout.EndSessionUseCase
import com.example.gymdiary3.domain.usecase.workout.LogSetUseCase
import com.example.gymdiary3.domain.usecase.workout.StartSessionUseCase
import com.example.gymdiary3.presentation.workout.ActiveWorkoutStateBuilder
import com.example.gymdiary3.presentation.workout.ActiveWorkoutUiState
import com.example.gymdiary3.presentation.workout.LoggerStateBuilder
import com.example.gymdiary3.presentation.workout.LoggerUiState
import com.example.gymdiary3.presentation.workout.PickerStateBuilder
import com.example.gymdiary3.presentation.workout.PickerUiState
import com.example.gymdiary3.system.session.SessionManager
import com.example.gymdiary3.system.timer.RestTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Rest timer as the UI needs it. */
data class RestUi(val running: Boolean, val secondsLeft: Int, val totalSeconds: Int)

/** Seconds since the active session started, ticking once a second; 0 when none. */
@OptIn(ExperimentalCoroutinesApi::class)
fun sessionElapsedSeconds(sessionManager: SessionManager, repo: WorkoutRepository): Flow<Long> =
    sessionManager.currentSessionId.flatMapLatest { id ->
        if (id == null) flowOf(0L)
        else repo.getSessionFlowById(id).flatMapLatest { session ->
            if (session == null) flowOf(0L)
            else flow {
                while (true) {
                    emit(((System.currentTimeMillis() - session.startTime) / 1000).coerceAtLeast(0))
                    delay(1000)
                }
            }
        }
    }

fun restUi(timer: RestTimerManager): Flow<RestUi> = combine(
    timer.isRestTimerRunning, timer.restTimerSeconds, timer.restTimerTotalSeconds
) { running, left, total -> RestUi(running, left, total) }

/* ------------------------------------------------------------ session overview */

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager,
    private val startSessionUseCase: StartSessionUseCase,
    private val endSessionUseCase: EndSessionUseCase,
    private val restTimer: RestTimerManager,
) : ViewModel() {

    init {
        viewModelScope.launch { sessionManager.initialize() }
    }

    val unit: StateFlow<String> = settingsRepository.userSettingsFlow.map { it.weightUnit }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    val state: StateFlow<ActiveWorkoutUiState?> = combine(
        workoutRepository.getSessionsWithSets().map { WorkoutAnalyzer.filterValidSessions(it) },
        sessionManager.currentSessionId,
        unit
    ) { sessions, active, u -> ActiveWorkoutStateBuilder.build(sessions, active, u, System.currentTimeMillis()) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val elapsedSeconds: StateFlow<Long> = sessionElapsedSeconds(sessionManager, workoutRepository)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val rest: StateFlow<RestUi> = restUi(restTimer)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RestUi(false, 0, 0))

    fun start() { viewModelScope.launch { startSessionUseCase() } }
    fun finish(onDone: (Int) -> Unit) { viewModelScope.launch { restTimer.skipTimer(); endSessionUseCase(onDone) } }
    fun adjustRest(delta: Int) = restTimer.adjust(delta)
    fun skipRest() = restTimer.skipTimer()
}

/* ---------------------------------------------------------------------- logger */

@HiltViewModel
class LoggerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager,
    private val logSetUseCase: LogSetUseCase,
    private val startSessionUseCase: StartSessionUseCase,
    private val restTimer: RestTimerManager,
) : ViewModel() {

    val exercise: String = Uri.decode(savedStateHandle.get<String>("exercise").orEmpty())
    val muscle: String = Uri.decode(savedStateHandle.get<String>("muscle").orEmpty())

    init {
        viewModelScope.launch { sessionManager.initialize() }
    }

    val settings: StateFlow<UserSettings> = settingsRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val state: StateFlow<LoggerUiState?> = combine(
        workoutRepository.getAllSets().map { all -> all.filter { it.exercise == exercise } },
        sessionManager.currentSessionId,
        settings
    ) { sets, active, s ->
        LoggerStateBuilder.build(exercise, muscle, sets, active, s.weightUnit, System.currentTimeMillis())
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rest: StateFlow<RestUi> = restUi(restTimer)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RestUi(false, 0, 0))

    /**
     * Logs a set. If no workout is running (e.g. the app was killed and the
     * session auto-closed), one is started first instead of dropping the set.
     */
    fun log(weightKg: Double, reps: Int, setNumber: Int, rpe: Float?, assisted: Boolean, notes: String?) {
        viewModelScope.launch {
            if (sessionManager.currentSessionId.value == null) startSessionUseCase()
            val sessionId = sessionManager.currentSessionId.value ?: return@launch
            logSetUseCase(
                sessionId = sessionId, muscle = muscle, exercise = exercise, setNumber = setNumber,
                reps = reps, weight = weightKg, isAssisted = assisted, rpe = rpe, notes = notes
            )
        }
    }

    fun deleteSet(id: Int) { viewModelScope.launch { workoutRepository.deleteSetById(id) } }
    fun adjustRest(delta: Int) = restTimer.adjust(delta)
    fun skipRest() = restTimer.skipTimer()
}

/* ---------------------------------------------------------------------- picker */

@HiltViewModel
class PickerViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    workoutRepository: WorkoutRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val query = MutableStateFlow("")
    val muscle = MutableStateFlow<String?>(null)

    val state: StateFlow<PickerUiState?> = combine(
        exerciseRepository.getAllExercisesFlow(),
        workoutRepository.getAllSets(),
        query,
        muscle,
        settingsRepository.userSettingsFlow
    ) { library, sets, q, m, s ->
        PickerStateBuilder.build(library, sets, q, m, s.weightUnit, System.currentTimeMillis())
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun add(name: String, muscle: String, equipment: EquipmentType, pattern: MovementPattern, tracking: TrackingType) {
        val clean = name.trim().split(Regex("\\s+")).joinToString(" ") { w ->
            w.lowercase().replaceFirstChar { it.uppercase() }
        }
        if (clean.isBlank()) return
        viewModelScope.launch {
            exerciseRepository.insertExercise(
                Exercise(
                    name = clean, primaryMuscleGroup = muscle, equipment = equipment,
                    movementPattern = pattern, trackingType = tracking, isCustom = true
                )
            )
        }
    }

    fun delete(exercise: Exercise) { viewModelScope.launch { exerciseRepository.deleteExercise(exercise) } }
}
