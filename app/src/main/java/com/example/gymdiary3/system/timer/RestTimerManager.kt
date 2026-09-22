package com.example.gymdiary3.system.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RestTimerManager {
    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var restTimerJob: Job? = null

    fun startTimer(seconds: Int = 90) {
        restTimerJob?.cancel()
        if (seconds <= 0) {
            skipTimer()
            return
        }
        // Drive the countdown from an absolute end time so the displayed value stays
        // accurate even if individual ticks are delayed (drift-free, self-correcting).
        val endTimeMs = System.currentTimeMillis() + seconds * 1000L
        _restTimerSeconds.value = seconds
        _isRestTimerRunning.value = true
        restTimerJob = managerScope.launch {
            while (true) {
                val remainingMs = endTimeMs - System.currentTimeMillis()
                if (remainingMs <= 0) break
                // Round up so the timer shows the full starting value and hits 0 exactly at the end.
                _restTimerSeconds.value = ((remainingMs + 999) / 1000).toInt()
                delay((remainingMs % 1000).let { if (it == 0L) 1000L else it })
            }
            _restTimerSeconds.value = 0
            _isRestTimerRunning.value = false
        }
    }

    fun skipTimer() {
        restTimerJob?.cancel()
        _restTimerSeconds.value = 0
        _isRestTimerRunning.value = false
    }
}
