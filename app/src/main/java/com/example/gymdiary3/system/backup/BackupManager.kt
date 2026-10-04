package com.example.gymdiary3.system.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import com.example.gymdiary3.domain.repository.ExerciseRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import com.example.gymdiary3.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BackupManager @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val bodyWeightRepository: BodyWeightRepository,
    private val exerciseRepository: ExerciseRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportJson(context: Context): Uri? = withContext(Dispatchers.IO) {
        try {
            val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val file = File(context.cacheDir, "gym_diary_backup_$dateStr.json")
            file.writeText(backupJson())
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Sessions, sets, body weight and the exercise library as JSON — what [exportJson] writes. */
    suspend fun backupJson(): String = withContext(Dispatchers.IO) {
        val sessions = workoutRepository.getSessionsWithSets().firstOrNull() ?: emptyList()
        val bodyWeights = bodyWeightRepository.getAllWeights()
        val exercises = exerciseRepository.getAllExercises()

        val backup = GymDiaryBackup(
            exportedAt = System.currentTimeMillis(),
            sessions = sessions.map { sws ->
                SessionBackup(
                    id = sws.session.id,
                    startTime = sws.session.startTime,
                    endTime = sws.session.endTime,
                    name = sws.session.name,
                    notes = sws.session.notes,
                    sets = sws.sets.map { s ->
                        SetBackup(s.setNumber, s.exercise, s.muscle, s.reps,
                                 s.weight, s.isAssisted, s.rpe, s.notes, s.timestamp)
                    }
                )
            },
            bodyWeights = bodyWeights.map { BodyWeightBackup(it.timestamp, it.weight) },
            exercises = exercises.map { e ->
                ExerciseBackup(e.name, e.primaryMuscleGroup, e.equipment.name,
                              e.movementPattern.name, e.trackingType.name, e.isCustom)
            }
        )
        json.encodeToString(backup)
    }

    suspend fun importJson(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val content = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull() ?: return@withContext Result.failure(Exception("Could not read file"))
        restore(content)
    }

    /**
     * Merges a backup into what is already on the phone; anything already there
     * (same exercise name, same weigh-in time, same session start) is skipped, so
     * restoring twice adds nothing. The whole file is decoded and mapped before
     * the first write, so a file that cannot be read changes nothing. Enum values
     * this build does not know — a backup from a newer version — fall back to
     * defaults rather than stopping the restore halfway.
     */
    suspend fun restore(content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backup = json.decodeFromString<GymDiaryBackup>(content)
            val newExercises = backup.exercises.map { e ->
                Exercise(
                    name = e.name,
                    primaryMuscleGroup = e.primaryMuscleGroup,
                    secondaryMuscleGroups = emptyList(),
                    equipment = enumOr(e.equipment, EquipmentType.OTHER),
                    movementPattern = enumOr(e.movementPattern, MovementPattern.ISOLATION),
                    trackingType = enumOr(e.trackingType, TrackingType.WEIGHT_REPS),
                    isCustom = e.isCustom
                )
            }

            val exerciseNames = exerciseRepository.getAllExercises().map { it.name }.toHashSet()
            newExercises.forEach { if (exerciseNames.add(it.name)) exerciseRepository.insertExercise(it) }

            val weighIns = bodyWeightRepository.getAllWeights().map { it.timestamp }.toHashSet()
            backup.bodyWeights.forEach { bw ->
                if (weighIns.add(bw.timestamp)) bodyWeightRepository.insertWeight(BodyWeight(0, bw.timestamp, bw.weight))
            }

            val sessionStarts = (workoutRepository.getSessionsWithSets().firstOrNull() ?: emptyList())
                .map { it.session.startTime }.toHashSet()
            backup.sessions.forEach { s ->
                if (!sessionStarts.add(s.startTime)) return@forEach
                val sessionId = workoutRepository.insertSession(
                    WorkoutSession(0, s.startTime, s.endTime, s.name, s.notes)
                ).toInt()
                s.sets.forEach { set ->
                    workoutRepository.insertSet(
                        WorkoutSet(
                            0, set.timestamp, set.muscle, set.exercise,
                            set.setNumber, set.reps, set.weight, set.isAssisted,
                            sessionId, set.rpe, set.notes
                        )
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback
}
