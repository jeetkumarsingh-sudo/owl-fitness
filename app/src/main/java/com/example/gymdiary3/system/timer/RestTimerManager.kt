package com.example.gymdiary3.system.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.seconds

class RestTimerManager {
    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()

    /** The full length of the current rest, including adjustments — for the progress line. */
    private val _restTimerTotalSeconds = MutableStateFlow(0)
    val restTimerTotalSeconds: StateFlow<Int> = _restTimerTotalSeconds.asStateFlow()

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var restTimerJob: Job? = null

    fun startTimer(seconds: Int = 90) {
        restTimerJob?.cancel()
        _restTimerSeconds.value = seconds
        _restTimerTotalSeconds.value = seconds
        _isRestTimerRunning.value = true
        restTimerJob = managerScope.launch {
            while (_restTimerSeconds.value > 0) {
                delay(1.seconds)
                _restTimerSeconds.value -= 1
            }
            _isRestTimerRunning.value = false
        }
    }

    /** Add or remove time from a running rest (e.g. ±15 s). Reaching zero ends it. */
    fun adjust(deltaSeconds: Int) {
        if (!_isRestTimerRunning.value) return
        val next = (_restTimerSeconds.value + deltaSeconds).coerceAtLeast(0)
        _restTimerTotalSeconds.value = (_restTimerTotalSeconds.value + deltaSeconds).coerceAtLeast(next)
        if (next == 0) skipTimer() else _restTimerSeconds.value = next
    }

    fun skipTimer() {
        restTimerJob?.cancel()
        _restTimerSeconds.value = 0
        _isRestTimerRunning.value = false
    }
}
