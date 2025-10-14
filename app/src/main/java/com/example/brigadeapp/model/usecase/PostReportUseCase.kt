package com.example.brigadeapp.model.usecase

import com.example.brigadeapp.model.domain.Report
import com.example.brigadeapp.model.repository.interfaces.ReportRepository

class PostReportUseCase(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(report: Report): Result<Unit> {
        return try {
            repository.submitReport(report)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
