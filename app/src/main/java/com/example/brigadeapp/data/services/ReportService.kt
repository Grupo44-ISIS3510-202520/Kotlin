package com.example.brigadeapp.data.services

import com.example.brigadeapp.domain.entity.Report

interface ReportService {
    suspend fun saveReport(report: Report): Result<Unit>
}