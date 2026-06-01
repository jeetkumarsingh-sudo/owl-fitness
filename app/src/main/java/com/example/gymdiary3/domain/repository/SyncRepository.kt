package com.example.gymdiary3.domain.repository

interface SyncRepository {
    suspend fun syncToCloud(): Result<Unit>
    suspend fun syncFromCloud(): Result<Unit>
    fun isSyncEnabled(): Boolean
}
