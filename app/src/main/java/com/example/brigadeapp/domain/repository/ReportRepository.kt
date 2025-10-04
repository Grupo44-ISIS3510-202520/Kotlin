package com.example.brigadeapp.domain.repository

import android.net.Uri
import com.example.brigadeapp.domain.model.Report

interface ReportRepository {
    suspend fun submitReport(report: Report)
}
