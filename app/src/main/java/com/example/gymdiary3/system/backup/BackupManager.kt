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

            val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val file = File(context.cacheDir, "gym_diary_backup_$dateStr.json")
            file.writeText(json.encodeToString(backup))
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importJson(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                ?: return@withContext Result.failure(Exception("Could not read file"))

            val backup = json.decodeFromString<GymDiaryBackup>(content)
            
            // Merge Exercises
            backup.exercises.forEach { e ->
                val existing = exerciseRepository.getAllExercises()
                if (existing.none { it.name == e.name }) {
                    exerciseRepository.insertExercise(
                        Exercise(
                            name = e.name,
                            primaryMuscleGroup = e.primaryMuscleGroup,
                            secondaryMuscleGroups = emptyList(),
                            equipment = EquipmentType.valueOf(e.equipment),
                            movementPattern = MovementPattern.valueOf(e.movementPattern),
                            trackingType = TrackingType.valueOf(e.trackingType),
                            isCustom = e.isCustom
                        )
                    )
                }
            }

            // Merge BodyWeights
            backup.bodyWeights.forEach { bw ->
                val existing = bodyWeightRepository.getWeights().firstOrNull() ?: emptyList()
                if (existing.none { it.timestamp == bw.timestamp }) {
                    bodyWeightRepository.insertWeight(BodyWeight(0, bw.timestamp, bw.weight))
                }
            }

            // Merge Sessions & Sets
            backup.sessions.forEach { s ->
                val existingSessions = workoutRepository.getSessionsWithSets().firstOrNull() ?: emptyList()
                if (existingSessions.none { it.session.startTime == s.startTime }) {
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
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
