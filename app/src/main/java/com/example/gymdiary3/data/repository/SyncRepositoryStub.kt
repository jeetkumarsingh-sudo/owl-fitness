package com.example.gymdiary3.data.repository

import com.example.gymdiary3.domain.repository.SyncRepository
import javax.inject.Inject

class SyncRepositoryStub @Inject constructor() : SyncRepository {
    override suspend fun syncToCloud(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Cloud sync not yet implemented")
    )
    override suspend fun syncFromCloud(): Result<Unit> = Result.failure(
        UnsupportedOperationException("Cloud sync not yet implemented")
    )
    override fun isSyncEnabled(): Boolean = false
}
