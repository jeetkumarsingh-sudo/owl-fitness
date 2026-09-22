package com.example.gymdiary3.system.timer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.gymdiary3.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Rest timer with an audible + haptic alert on completion so you don't have to
 * watch the screen between sets. The countdown is driven from an absolute end
 * time, so it stays accurate even if ticks are delayed, and it can be extended
 * or shortened on the fly.
 */
class RestTimerManager(private val context: Context) {

    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var restTimerJob: Job? = null

    @Volatile
    private var endTimeMs: Long = 0L

    fun startTimer(seconds: Int = 90) {
        restTimerJob?.cancel()
        if (seconds <= 0) {
            skipTimer()
            return
        }
        endTimeMs = System.currentTimeMillis() + seconds * 1000L
        _restTimerSeconds.value = seconds
        _isRestTimerRunning.value = true
        restTimerJob = managerScope.launch {
            while (true) {
                val remainingMs = endTimeMs - System.currentTimeMillis()
                if (remainingMs <= 0) break
                // Round up so the display shows the full starting value and hits 0 at the end.
                _restTimerSeconds.value = ((remainingMs + 999) / 1000).toInt()
                delay((remainingMs % 1000).let { if (it == 0L) 1000L else it })
            }
            _restTimerSeconds.value = 0
            _isRestTimerRunning.value = false
            notifyComplete()
        }
    }

    /** Extend (or, with a negative delta, shorten) the running timer by [deltaSeconds]. */
    fun addTime(deltaSeconds: Int) {
        if (!_isRestTimerRunning.value) return
        val now = System.currentTimeMillis()
        endTimeMs = (endTimeMs + deltaSeconds * 1000L).coerceAtLeast(now)
        _restTimerSeconds.value = (((endTimeMs - now) + 999) / 1000).toInt()
    }

    fun skipTimer() {
        restTimerJob?.cancel()
        _restTimerSeconds.value = 0
        _isRestTimerRunning.value = false
    }

    // --- Completion alert -----------------------------------------------------

    private fun notifyComplete() {
        vibrate()
        playTone()
        postNotification()
    }

    private fun vibrate() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return
            if (!vibrator.hasVibrator()) return
            val pattern = longArrayOf(0, 150, 120, 150)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (_: Exception) {
        }
    }

    private fun playTone() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
            managerScope.launch {
                delay(600)
                tone.release()
            }
        } catch (_: Exception) {
        }
    }

    private fun postNotification() {
        try {
            ensureChannel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Rest complete")
                .setContentText("Time for your next set.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTimeoutAfter(15_000)
                .build()
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {
        }
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rest timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts when your rest timer finishes." }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "rest_timer"
        private const val NOTIFICATION_ID = 4201
    }
}
