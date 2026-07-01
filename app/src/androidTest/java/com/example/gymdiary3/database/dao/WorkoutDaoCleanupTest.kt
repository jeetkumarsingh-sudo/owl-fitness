package com.example.gymdiary3.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.core.database.WorkoutDatabase
import com.example.gymdiary3.core.database.dao.WorkoutDao
import com.example.gymdiary3.core.database.entity.WorkoutSessionEntity
import com.example.gymdiary3.core.database.entity.WorkoutSetEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDaoCleanupTest {
    private lateinit var database: WorkoutDatabase
    private lateinit var workoutDao: WorkoutDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        workoutDao = database.workoutDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deleteEmptySessions_keepsActiveEmptySessionsAndCompletedSessionsWithSets() = runBlocking {
        val activeEmptySessionId = workoutDao.insertSession(
            WorkoutSessionEntity(startTime = 1_000L, endTime = null)
        ).toInt()
        val completedEmptySessionId = workoutDao.insertSession(
            WorkoutSessionEntity(startTime = 2_000L, endTime = 3_000L)
        ).toInt()
        val completedSessionWithSetsId = workoutDao.insertSession(
            WorkoutSessionEntity(startTime = 4_000L, endTime = 5_000L)
        ).toInt()

        workoutDao.insertWorkout(
            WorkoutSetEntity(
                timestamp = 4_500L,
                muscle = "Back",
                exercise = "Pull-Ups",
                setNumber = 1,
                reps = 8,
                weight = 0.0,
                isAssisted = false,
                sessionId = completedSessionWithSetsId,
            )
        )

        workoutDao.deleteEmptySessions()

        assertNotNull(workoutDao.getSessionById(activeEmptySessionId))
        assertNull(workoutDao.getSessionById(completedEmptySessionId))
        assertNotNull(workoutDao.getSessionById(completedSessionWithSetsId))
    }
}
