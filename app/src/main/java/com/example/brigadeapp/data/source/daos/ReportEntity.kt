package com.example.brigadeapp.data.source.daos

import com.example.brigadeapp.domain.entity.Report

/**
 * Compatibility mapper for older code that referenced ReportEntity.
 * The domain `Report` is now the canonical Room entity; this file
 * keeps mapping helpers in case other modules still call them.
 */
object ReportEntityMapper {
    fun toDomain(dummy: Any?): Report? = null

    fun fromDomain(report: Report): Map<String, Any?> = mapOf(
        "id" to report.id,
        "type" to report.type,
        "place" to report.place,
        "time" to report.time,
        "description" to report.description,
        "followUp" to report.followUp,
        "imageUrl" to report.imageUrl,
        "audioUrl" to report.audioUrl,
        "timestamp" to report.timestamp,
        "elapsedTime" to report.elapsedTime,
        "synced" to report.synced
    )
}
