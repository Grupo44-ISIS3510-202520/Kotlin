package com.example.brigadeapp.data.services.local

import android.content.Context
import android.util.Log
import com.example.brigadeapp.data.source.local.AppDatabase
import com.example.brigadeapp.data.services.ReportService
import com.example.brigadeapp.domain.entity.Report
import com.example.brigadeapp.domain.entity.CachedReport
import javax.inject.Inject

class ReportLocalService @Inject constructor(
	private val context: Context
): ReportService {
	private val db by lazy { AppDatabase.getInstance(context) }
	private val dao by lazy { db.reportDao() }
	private val cachedReportDao by lazy { db.cachedReportDao() }

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

	suspend fun cacheReports(reports: List<CachedReport>): Result<Unit> {
		return try {
			cachedReportDao.deleteSynced()
			cachedReportDao.insertAll(reports)
			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getCachedReports(limit: Int = 10): Result<List<CachedReport>> {
		return try {
			Result.success(cachedReportDao.getLatest(limit))
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	fun observeCachedReports(limit: Int = 10): kotlinx.coroutines.flow.Flow<List<CachedReport>> {
		return cachedReportDao.observeLatest(limit)
	}

	suspend fun saveToCachedReports(report: Report, reportId: String): Result<Unit> {
		return try {
			val cachedReport = CachedReport(
				reportId = reportId,
				type = report.type,
				place = report.place,
				description = report.description,
				imageUrl = report.imageUrl,
				audioUrl = report.audioUrl,
				isFollowUp = report.followUp,
				timestamp = report.timestamp,
				elapsedTime = report.elapsedTime,
				latitude = report.latitude,
				longitude = report.longitude,
				userId = report.userId,
				synced = report.synced
			)
			cachedReportDao.insert(cachedReport)
			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun deletePendingCachedReport(reportId: String): Result<Unit> {
		return try {
			cachedReportDao.deleteById(reportId)
			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}
}
