package com.example.gymdiary3.data.mapper

import com.example.gymdiary3.core.database.entity.BodyWeightEntity
import com.example.gymdiary3.core.database.entity.WorkoutSessionEntity
import com.example.gymdiary3.core.database.entity.WorkoutSetEntity
import com.example.gymdiary3.core.database.relation.SessionWithSets as SessionWithSetsRelation
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.model.SessionWithSets

fun WorkoutSetEntity.toDomain(): WorkoutSet = WorkoutSet(
    id = id,
    timestamp = timestamp,
    muscle = muscle,
    exercise = exercise,
    setNumber = setNumber,
    reps = reps,
    weight = weight,
    isAssisted = isAssisted,
    sessionId = sessionId,
    rpe = rpe,
    notes = notes
)

fun WorkoutSet.toEntity(): WorkoutSetEntity = WorkoutSetEntity(
    id = id,
    timestamp = timestamp,
    muscle = muscle,
    exercise = exercise,
    setNumber = setNumber,
    reps = reps,
    weight = weight,
    isAssisted = isAssisted,
    sessionId = sessionId,
    rpe = rpe,
    notes = notes
)

fun WorkoutSessionEntity.toDomain(): WorkoutSession = WorkoutSession(
    id = id,
    startTime = startTime,
    endTime = endTime,
    name = name,
    notes = notes
)

fun WorkoutSession.toEntity(): WorkoutSessionEntity = WorkoutSessionEntity(
    id = id,
    startTime = startTime,
    endTime = endTime,
    name = name,
    notes = notes
)

fun BodyWeightEntity.toDomain(): BodyWeight = BodyWeight(
    id = id,
    timestamp = timestamp,
    weight = weight
)

fun BodyWeight.toEntity(): BodyWeightEntity = BodyWeightEntity(
    id = id,
    timestamp = timestamp,
    weight = weight
)

fun SessionWithSetsRelation.toDomain(): SessionWithSets = SessionWithSets(
    session = session.toDomain(),
    sets = sets.map { it.toDomain() }
)
