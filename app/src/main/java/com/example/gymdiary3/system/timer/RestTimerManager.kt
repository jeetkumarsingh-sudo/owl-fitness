package com.example.gymdiary3.system.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RestTimerManager(private val scope: CoroutineScope) {
    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()

    private var restTimerJob: Job? = null

    fun startTimer(seconds: Int = 90) {
        restTimerJob?.cancel()
        _restTimerSeconds.value = seconds
        _isRestTimerRunning.value = true
        restTimerJob = scope.launch {
            while (_restTimerSeconds.value > 0) {
                delay(1000L)
                _restTimerSeconds.value -= 1
            }
            _isRestTimerRunning.value = false
        }
    }

    fun skipTimer() {
        restTimerJob?.cancel()
        _restTimerSeconds.value = 0
        _isRestTimerRunning.value = false
    }
}
