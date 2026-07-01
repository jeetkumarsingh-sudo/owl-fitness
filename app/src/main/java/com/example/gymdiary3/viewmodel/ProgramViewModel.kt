package com.example.gymdiary3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymdiary3.domain.model.*
import com.example.gymdiary3.domain.repository.ProgramRepository
import com.example.gymdiary3.system.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val allProgramDays = programRepository.getAllProgramDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledSessions = programRepository.getScheduledSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedDefaultProgramIfEmpty()
    }

    private fun seedDefaultProgramIfEmpty() {
        viewModelScope.launch {
            val days = allProgramDays.first()
            if (days.isEmpty()) {
                seedProgram()
            }
        }
    }

    private suspend fun seedProgram() {
        val days = listOf(
            ProgramDay(name = "Day 1 — Push", dayNumber = 1, sessionType = "Push", plannedDuration = 70, primaryPriority = "Upper Chest, Side Delts, OHP", warmupNotes = "5 min easy bike/treadmill + Band pull-aparts 2x20 + Chin tucks 2x15 + Compound ramp"),
            ProgramDay(name = "Day 2 — Pull", dayNumber = 2, sessionType = "Pull", plannedDuration = 75, primaryPriority = "Lats, Face Pulls, Biceps", warmupNotes = "5 min easy bike/treadmill + Band pull-aparts 2x20 + Chin tucks 2x15 + Compound ramp"),
            ProgramDay(name = "Day 3 — Legs", dayNumber = 3, sessionType = "Legs", plannedDuration = 70, primaryPriority = "Squat, RDL, Quad isolation", warmupNotes = "Clamshells 2x15 + Ankle mob 10/side + 5 min easy bike/treadmill + Band pull-aparts 2x20 + Chin tucks 2x15 + Compound ramp"),
            ProgramDay(name = "Day 4 — Rest", dayNumber = 4, sessionType = "Rest", plannedDuration = 20, primaryPriority = "Recovery", warmupNotes = "20 min walk only"),
            ProgramDay(name = "Day 5 — Upper Volume", dayNumber = 5, sessionType = "Upper Volume", plannedDuration = 70, primaryPriority = "Side Delts, Rear Delts, Arms, 2nd Lat session", warmupNotes = "5 min easy bike/treadmill + Band pull-aparts 2x20 + Chin tucks 2x15 + Compound ramp"),
            ProgramDay(name = "Day 6 — Light Lower", dayNumber = 6, sessionType = "Light Lower", plannedDuration = 50, primaryPriority = "Technique + Mobility", warmupNotes = "If sleep <7 hrs for 3+ nights -> Full Rest. Otherwise: Clamshells 2x15 + Ankle mob 10/side + 5 min easy bike/treadmill"),
            ProgramDay(name = "Day 7 — Rest", dayNumber = 7, sessionType = "Rest", plannedDuration = 0, primaryPriority = "Full Recovery", warmupNotes = "Full rest. No training.")
        )

        days.forEach { day ->
            val dayId = programRepository.insertProgramDay(day).toInt()
            seedExercisesForDay(dayId, day.sessionType)
        }
    }

    private suspend fun seedExercisesForDay(dayId: Int, type: String) {
        val exercises = when (type) {
            "Push" -> listOf(
                ProgramExercise(programDayId = dayId, exerciseName = "Push-ups", order = 1, setsPlanned = 2, repsPlanned = "12–15", restSeconds = 60, notes = "ACTIVATION ONLY. 3-count lowering. Hands at 45°.", category = "Warm-up"),
                ProgramExercise(programDayId = dayId, exerciseName = "Incline Smith / DB Press", order = 2, setsPlanned = 4, repsPlanned = "8–10", restSeconds = 120, notes = "Bench 30–40°. Scapula retracted. Touch upper chest.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "OHP (Strict)", order = 3, setsPlanned = 3, repsPlanned = "6–8", restSeconds = 120, progressionRule = "Add 2.5 kg when 3x8 at RIR 2 reached.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Dumbbell Lateral Raises", order = 4, setsPlanned = 3, repsPlanned = "12–15", restSeconds = 75, notes = "Lead with elbow. Stop at shoulder height. Zero shrugging.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Flat Bench or Chest Dips", order = 5, setsPlanned = 3, repsPlanned = "5–7", restSeconds = 150, notes = "Month 1: Flat bench 40 kg. Month 2+: Weighted dips.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Pec Deck / Cable Fly", order = 6, setsPlanned = 3, repsPlanned = "12–15", restSeconds = 75, notes = "1-second pause at peak contraction.", category = "Finisher"),
                ProgramExercise(programDayId = dayId, exerciseName = "Skull Crushers", order = 7, setsPlanned = 3, repsPlanned = "10–12", restSeconds = 75, notes = "Elbows fully tucked. Full extension at top.", category = "Accessory")
            )
            "Pull" -> listOf(
                ProgramExercise(programDayId = dayId, exerciseName = "Pull-Ups (overhand)", order = 1, setsPlanned = 3, repsPlanned = "max (cap 8)", restSeconds = 120, notes = "Dead hang start. Elbows DOWN toward hips.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Deadlift", order = 2, setsPlanned = 3, repsPlanned = "70%x5, 80%x5, Top: workingx5", restSeconds = 180, notes = "Brace fully. Add 5 kg to top set when all 3 clean.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Lat Pulldown (Wide)", order = 3, setsPlanned = 3, repsPlanned = "10–12", restSeconds = 90, notes = "Pull to upper chest. Elbows to back pockets.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Straight Arm Pulldown", order = 4, setsPlanned = 3, repsPlanned = "12–15", restSeconds = 75, notes = "Arms STRAIGHT. Best pure lat isolation.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Face Pulls", order = 5, setsPlanned = 3, repsPlanned = "15", restSeconds = 60, notes = "Elbows high and wide. External rotation at peak.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Barbell Curl", order = 6, setsPlanned = 3, repsPlanned = "8–10", restSeconds = 90, notes = "2-up 3-down tempo. Elbows pinned to sides.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Hammer Curl", order = 7, setsPlanned = 2, repsPlanned = "10–12", restSeconds = 60, notes = "Neutral grip. Builds brachialis thickness.", category = "Accessory")
            )
            "Legs" -> listOf(
                ProgramExercise(programDayId = dayId, exerciseName = "Squat", order = 1, setsPlanned = 4, repsPlanned = "5–6", restSeconds = 180, notes = "Ramp: 60%x8, 70%x6, 80%x6, 85%x5. Add 2.5 kg when 4x6 at RIR 2.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Leg Press", order = 2, setsPlanned = 3, repsPlanned = "10–12", restSeconds = 120, notes = "Full depth. Medium-high foot placement.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "RDL", order = 3, setsPlanned = 3, repsPlanned = "8–10", restSeconds = 120, notes = "Hip hinge, soft knees. Stop at full stretch.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Leg Extension", order = 4, setsPlanned = 3, repsPlanned = "12", restSeconds = 75, notes = "1-second squeeze at top. 3-count lowering.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Leg Curl", order = 5, setsPlanned = 3, repsPlanned = "10–12", restSeconds = 75, notes = "Hamstring contraction to complement RDL stretch.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Standing Calf Raises", order = 6, setsPlanned = 4, repsPlanned = "15–20", restSeconds = 60, notes = "2-up, 3-down, 1-second full stretch at bottom.", category = "Accessory")
            )
            "Upper Volume" -> listOf(
                ProgramExercise(programDayId = dayId, exerciseName = "Cable Lateral Raises (single)", order = 1, setsPlanned = 3, repsPlanned = "12–15", restSeconds = 60, notes = "Cable at floor. Elbow leads to shoulder height.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Dumbbell Lateral Raises", order = 2, setsPlanned = 3, repsPlanned = "12–15", restSeconds = 60, notes = "Different tension curve from cables.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Face Pulls", order = 3, setsPlanned = 3, repsPlanned = "15", restSeconds = 60, notes = "Identical execution to Day 2.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Low-to-High Cable Fly", order = 4, setsPlanned = 3, repsPlanned = "12–15", restSeconds = 75, notes = "Directly isolates clavicular pec head.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Lat Pulldown (Neutral Close)", order = 5, setsPlanned = 3, repsPlanned = "10–12", restSeconds = 90, notes = "Emphasizes lower lat.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Incline Dumbbell Curl", order = 6, setsPlanned = 3, repsPlanned = "10–12", restSeconds = 75, notes = "45° bench. Arms hang behind torso.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Rope Pushdown + Extension", order = 7, setsPlanned = 5, repsPlanned = "12-15 / 10-12", restSeconds = 60, notes = "Pushdown: lateral head. Extension: long head.", category = "Accessory")
            )
            "Light Lower" -> listOf(
                ProgramExercise(programDayId = dayId, exerciseName = "Light Squat (Tempo)", order = 1, setsPlanned = 3, repsPlanned = "12", restSeconds = 90, notes = "50 kg max. 3-down, 1s hold, 2-up. Pure technique.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Light RDL", order = 2, setsPlanned = 3, repsPlanned = "10", restSeconds = 90, notes = "40 kg. Feel every hamstring stretch.", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Nordic Hamstring Curl", order = 3, setsPlanned = 3, repsPlanned = "6–8", restSeconds = 90, notes = "Controlled lowering phase (4–5 seconds).", category = "Primary"),
                ProgramExercise(programDayId = dayId, exerciseName = "Calf Raises", order = 4, setsPlanned = 3, repsPlanned = "20", restSeconds = 60, notes = "Same execution as Day 3.", category = "Accessory"),
                ProgramExercise(programDayId = dayId, exerciseName = "Ab Circuit", order = 5, setsPlanned = 3, repsPlanned = "Hanging/Crunches/Plank", restSeconds = 60, notes = "10 knee raises + 15 crunches + 30s plank.", category = "Accessory")
            )
            else -> emptyList()
        }

        exercises.forEach { programRepository.insertProgramExercise(it) }
    }

    fun scheduleSession(day: ProgramDay, date: Long) {
        viewModelScope.launch {
            programRepository.insertScheduledSession(
                SessionSchedule(
                    title = day.name,
                    date = date,
                    programDayId = day.id,
                    status = "Planned"
                )
            )
        }
    }

    fun getExercisesForDay(dayId: Int): Flow<List<ProgramExercise>> {
        return programRepository.getExercisesForDay(dayId)
    }

    fun logScheduledSession(schedule: SessionSchedule, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            // 1. Use SessionManager to start the session correctly
            sessionManager.startSession(
                sessionDateMillis = System.currentTimeMillis(),
                name = schedule.title,
                notes = schedule.notes
            )
            val sessionId = sessionManager.currentSessionId.value ?: return@launch

            // 2. Mark schedule as Done
            programRepository.updateScheduledSession(schedule.copy(status = "Done"))

            // 3. Pre-populate Session Exercise Logs if programDayId is available
            schedule.programDayId?.let { dayId ->
                programRepository.getExercisesForDay(dayId).first().forEach { exercise ->
                    programRepository.insertSessionExerciseLog(
                        SessionExerciseLog(
                            sessionId = sessionId,
                            programExerciseId = exercise.id,
                            exerciseName = exercise.exerciseName,
                            order = exercise.order
                        )
                    )
                }
            }

            onComplete(sessionId)
        }
    }

    fun getLogsForSession(sessionId: Int): Flow<List<SessionExerciseLog>> {
        return programRepository.getLogsForSession(sessionId)
    }

    fun updateExerciseLog(log: SessionExerciseLog) {
        viewModelScope.launch {
            programRepository.insertSessionExerciseLog(log)
        }
    }
}
