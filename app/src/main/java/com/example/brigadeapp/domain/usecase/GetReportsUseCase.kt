package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.domain.entity.CachedReport
import com.example.brigadeapp.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow

class GetReportsUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(limit: Int = 10): Result<List<CachedReport>> {
        return repository.getLatestReports(limit)
    }

    fun observe(limit: Int = 10): Flow<Result<List<CachedReport>>> {
        return repository.observeReports(limit)
    }
}
