package com.example.gymdiary3.system.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.gymdiary3.domain.repository.BodyWeightRepository
import com.example.gymdiary3.domain.repository.ExerciseRepository
import com.example.gymdiary3.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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
            
            // TODO: Implementation of merging data
            // This would involve batch inserts into repositories
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
