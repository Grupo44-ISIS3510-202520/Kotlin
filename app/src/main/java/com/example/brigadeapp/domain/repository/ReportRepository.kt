package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.entity.Report

interface ReportRepository {
    suspend fun submitReport(report: Report)
}
