package com.example.brigadeapp.data.services.local

import android.content.Context
import com.example.brigadeapp.data.source.local.AppDatabase
import com.example.brigadeapp.data.services.ReportService
import com.example.brigadeapp.domain.entity.Report
import javax.inject.Inject

class ReportLocalService @Inject constructor(
	private val context: Context
): ReportService {
	private val db by lazy { AppDatabase.getInstance(context) }
	private val dao by lazy { db.reportDao() }

	override suspend fun saveReport(report: Report): Result<Unit> {
		return try {
			dao.insert(report)
			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getUnsynced(): Result<List<Report>> {
		return try {
			Result.success(dao.getUnsynced())
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun markSynced(id: Long) {
		try {
			dao.markSynced(id, true)
		} catch (e: Exception) {
            throw Exception("Error marking report as synced: ${e.message}")
        }
	}
}
