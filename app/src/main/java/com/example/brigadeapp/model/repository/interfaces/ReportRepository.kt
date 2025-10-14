package com.example.brigadeapp.model.repository.interfaces

import com.example.brigadeapp.model.domain.Report

interface ReportRepository {
    suspend fun submitReport(report: Report)
}
