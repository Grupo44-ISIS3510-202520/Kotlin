package com.example.brigadeapp.domain.usecase

import android.net.Uri
import com.example.brigadeapp.domain.model.Report
import com.example.brigadeapp.domain.repository.ReportRepository

class SubmitReportUseCase(
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
